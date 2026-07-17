# 多属性源兼容 API

来源键（source key）用于将装备、职业、药水、脚本和第三方插件的属性隔离保存。同名键重复写入会替换旧值，不同键互不影响；装备刷新只重建装备自身来源。

## 来源键示例

| 键 | 用途 |
|---|---|
| `装备-主手`、`装备-副手`、`装备-盔甲`、`槽位`、`RPG栏` | SX-Attribute 自动维护的装备来源。 |
| `力量加成` | 外部插件或脚本维护的自定义来源。 |
| `class:<全类名>` | 旧 `setEntityAPIData(Class, …)` 的兼容映射。 |
| `抛射物` | 弓箭等投射物的属性快照。 |

## 获取 API

```java
SXAPI api = SXAttribute.getApi();
```

| 方法 | 说明 |
|---|---|
| `addSourceAttribute(entity, source, lore/data, update)` | 解析 Lore 或直接写入数据；同名来源会覆盖。 |
| `createStaticAttributeSource(entity, source, lore/data, update)` | 写入静态数值来源，不触发来源变更事件。 |
| `takeSourceAttribute(entity, source[, update])` | 移除指定来源并返回原数据。 |
| `getSourceAttribute(uuid, source)` / `hasSourceAttribute(uuid, source)` | 查询指定来源。 |
| `getSourceNames(uuid)` | 查询实体全部来源键。 |
| `loadListData(lore)` | 将属性 Lore 解析为 `SXAttributeData`。 |

`update` 为 `true` 时立即刷新生命、速度等 `UPDATE` 属性；战斗概率类属性会在事件发生时读取。

## 示例

```java
SXAPI api = SXAttribute.getApi();

api.addSourceAttribute(player, "力量加成",
        java.util.Arrays.asList("攻击力: 50", "暴击几率: 10"), true);

// 只删除该来源，不影响装备、职业或其它插件来源。
api.takeSourceAttribute(player, "力量加成", true);
```

新增来源会触发可取消的 `SXAttributeSourceAddEvent`，移除来源会通知 `SXAttributeSourceRemoveEvent`。新项目如需持续时间、叠层、持久化或多服同步，优先使用 [属性来源与多服存储](../features/source.md) 的受管来源 API。

## 兼容持久化指令

| 指令 | 说明 |
|---|---|
| `/sxa source [玩家]` | 查看各来源及其非零属性贡献。 |
| `/sxa statssource [玩家]` | 打开来源统计面板。 |
| `/sxa update [玩家]` | 重新扫描装备来源并刷新 UPDATE 属性。 |
| `/sxa persistent <玩家> <源名> <属性词条...>` | 添加兼容持久化来源。 |
| `/sxa del-persistent <玩家> <源名>` | 删除兼容持久化来源。 |

示例：`/sxa persistent Steve 力量加成 攻击力: 50, 暴击几率: 10`。
