# Java API 与事件

通过 `SXAttribute.getApi()` 取得 `SXAPI` 实例。API 同时保留旧版 Lore/命名来源接口，并提供动态属性、来源生命周期和装备操作入口。涉及 Bukkit 实体或物品的调用必须遵守服务端线程模型；Folia 环境请回到实体所属区域线程。

## 动态属性、来源与装备 API

```java
SXAPI api = SXAttribute.getApi();

double fireDamage = api.getDynamicAttribute(player, "FireDamage", "damage");
SourceWriteResult result = api.applyManagedSourceRule(player, "BattleRage");
FeatureResult forge = api.operateEquipmentFeature(player, item, "Enhance");
api.triggerDynamicAttributes(player, target);
```

| 方法 | 用途 |
|---|---|
| `getDynamicAttribute(entity, attributeId, field)` | 读取 `Feature/Attribute` 定义字段的当前聚合值；不存在时返回 `0`。 |
| `applyManagedSource(entity, request)` | 以 `SourceApplyRequest` 施加受管来源。 |
| `applyManagedSourceRule(entity, ruleId)` | 按 `Feature/Source/Config.yml` 的 `Rules.<ID>` 施加来源。 |
| `removeManagedSource(entity, source)` | 删除受管来源。 |
| `operateEquipmentFeature(player, item, featureId)` | 不经过 GUI 直接运行装备模块操作；调用方负责物品位置和界面反馈。 |
| `getEquipmentFeatureState(item, featureId)` | 读取模块存储在物品 NBT 中的 YAML 状态。 |
| `setEquipmentFeatureState(player, item, featureId, state)` | 写入模块状态并触发该模块的 Lore 重建流程。 |
| `triggerDynamicAttributes(actor, target)` | 触发所有 `Event: API` 的动态属性规则。 |

受管来源返回 `SourceWriteResult`：`APPLIED`、`REMOVED`、`UNIQUE_EXISTS`、`REDIS_UNAVAILABLE`、`LOCK_CONFLICT`、`STORAGE_DISABLED`、`VERSION_CONFLICT` 或 `FAILED`。持久化来源在多服模式缺少 Redis 时会返回 `REDIS_UNAVAILABLE`，不应把它当作普通失败后继续本地写入。

## 兼容 API

| 方法 | 用途 |
|---|---|
| `addSourceAttribute` / `takeSourceAttribute` | 添加或移除命名 Lore 属性源。非静态新增可被事件取消。 |
| `createStaticAttributeSource` | 创建不触发来源事件的静态属性源，适合内部批量计算。 |
| `getSourceAttribute` / `getSourceNames` | 读取命名来源数据或来源名称。 |
| `loadListData` / `loadItemData` | 将 Lore 或预加载物品解析为 `SXAttributeData`。 |
| `getEntityData` / `updateData` / `attributeUpdate` | 读取实体数据、重新加载装备数据或刷新 UPDATE 类原版属性。 |
| `getItem` / `hasItem` / `getItemList` | 读取 `Item/` 模板物品及其索引。 |
| `setProjectileData` / `getProjectileData` | 保存或读取抛射物属性快照。 |

旧的 `Class<?>` 来源 API（`setEntityAPIData`、`getEntityAPIData` 等）仍可使用，内部会映射为 `class:<全类名>` 命名来源。新集成应优先使用显式来源名或 `SourceService`，避免不同插件共享同一个 Class 来源的语义不清晰。

## 事件总表

所有事件均使用 Bukkit `HandlerList`。`SXPreLoadItemEvent`、`SXGetAttributeEvent` 和 `SXLoadAttributeEvent` 的旧构造参数中仍保留 `isAsync`，但事件的异步标记始终由调用时的实际 Bukkit 线程决定，监听器应以 `Event#isAsynchronous()` 为准。

| 事件 | 时机与可变内容 |
|---|---|
| `SXDamageEvent` | SX 伤害数据计算完成时触发，携带 `DamageData`。 |
| `SXReloadEvent` | `/sxa reload` 重载完成时触发，携带执行者 `CommandSender`。 |
| `SXPreLoadItemEvent` | 实体装备进入属性解析前触发，携带实体与 `List<PreLoadItem>`。 |
| `SXGetAttributeEvent` | 读取实体最终属性时触发，可通过 `setData` 替换结果。 |
| `SXLoadAttributeEvent` | 加载实体装备属性后触发，携带实体、预加载物品和 `SXAttributeData`。 |
| `SXItemSpawnEvent` | 通过物品模板生成后触发，可通过 `setItem` 替换发放物品。 |
| `SXItemUpdateEvent` | 自动更新模板物品前触发，可取消并可替换新物品；同时可读取旧物品。 |
| `SXAttributeSourceAddEvent` | 非静态命名来源加入时触发，可取消或通过 `setData` 替换属性数据。 |
| `SXAttributeSourceRemoveEvent` | 非静态命名来源移除后通知，携带被移除的数据，不可取消。 |
| `SXAttributeActionEvent` | 动态属性动作前后触发；`PRE` 可取消单个动作，`POST` 仅通知。 |
| `SXSourceWriteEvent` | 受管来源写入、删除或被拒绝后通知，携带来源名与 `SourceWriteResult`。 |
| `SXForgeTransactionEvent` | 一次装备模块事务完成后通知，携带玩家、模块 ID、物品和 `FeatureResult`。 |
| `SXEnchantGrowthEvent` | 附魔经验或等级改变后通知，携带旧/新等级及本次获得经验。 |

监听器不得假设事件一定在主线程。尤其不要从异步事件直接修改实体、背包或世界；将这些操作调度回服务端要求的实体线程。
