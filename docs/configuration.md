# 主配置与安装说明

## 安装与依赖

将 SX-Attribute 与 **SX-Item** 放入 `plugins/` 后完整重启服务器。SX-Item 是必需依赖；Vault、PlaceholderAPI、HolographicDisplays、DecentHolograms、RPGInventory、MythicMobs、AuraSkills、SkillAPI、PacketEvents 与 ProtocolLib 为可选依赖。

`DamageEvent.DamageParticleLimit` 需要 PacketEvents 或 ProtocolLib 才能修改客户端收到的原版伤害指示粒子包。两者同时安装时优先 PacketEvents；PacketEvents 不可用或适配失败时自动回退 ProtocolLib。两者都未安装不会影响伤害结算和插件启动，只会保留原版粒子数量并在控制台提示。

首次启动会生成 `plugins/SX-Attribute/`。升级前请备份该目录、玩家物品和来源数据库；不要使用插件热卸载工具。

## `Config.yml`

| 配置路径 | 默认值 | 说明 |
|---|---:|---|
| `CommandStatsDisplaySkullSkin` | `false` | 属性面板头像是否读取玩家皮肤。 |
| `DecimalFormat` | `#.##` | 属性、成本和 Lore 数字格式。 |
| `Compatibility.MythicMobs` | `true` | 是否与 MythicMobs 的装备/掉落集成；同 ID 被两方识别时可能重复计算。改动后需重启。 |
| `Holographic.Enabled` | `true` | 是否显示伤害、暴击等全息信息。 |
| `Holographic.DisplayTime` | `2` | 全息显示秒数。 |
| `HealthDisplays.Name/BossBar` | - | 生命名称显示与 BossBar 显示；`BlackCauseList` 排除伤害原因。 |
| `ItemDisplayName` | `true` | 是否读取物品显示名。 |
| `DamageEvent.Priority` | `HIGH` | SX 伤害监听优先级；与其它战斗插件冲突时再调整。 |
| `DamageEvent.BlackCauseList` | `[CUSTOM]` | 不参与 SX 计算的 Bukkit 伤害原因。 |
| `DamageEvent.DamageCalculationToEVE` | `false` | 是否计算怪物对怪物伤害；不影响玩家攻击生物。 |
| `DamageEvent.DamageGauges` | `true` | 是否同步部分攻击伤害到原版攻击属性。 |
| `DamageEvent.BanShieldDefense` | `false` | 是否禁用盾牌防御相关处理。 |
| `DamageEvent.BowCloseRangeAttack` | `false` | 手持弓近战时是否使用弓的属性。 |
| `DamageEvent.MinimumDamage` | `1.0` | SX 计算后保留的最小伤害。 |
| `DamageEvent.DamageParticleLimit` | `8` | 发给客户端的 `DAMAGE_INDICATOR` 数量上限；优先 PacketEvents、回退 ProtocolLib。`-1` 不拦截，`0` 取消整个粒子包。只修改出站包，不修改伤害、生命或击杀判定。 |
| `ClearDefaultAttribute` | `true` | 是否清理物品默认原版属性修饰符，避免与 SX 数值重复。 |
| `RPGInventorySlot` | 列表 | RPGInventory 槽位编号。 |
| `RegisterSlots` | - | 玩家自身背包中的饰品槽位。每项格式为 `槽位#Lore识别名`，物品 Lore 须包含该名称。 |
| `DefaultAttribute` | `生命上限: 20` | 实体没有物品属性时的基础属性 Lore。 |
| `NBTAttribute.Nodes` | 装备模块的 `.Attributes` 节点 | 从物品 NBT 读取属性文本的路径列表；支持字符串、列表和嵌套映射。 |
| `EquipmentFeature.LoreMode` | `VARIABLE` | 装备拓展显示模式，可选 `LORE` 或 `VARIABLE`。 |
| `Condition` | - | 主副手、护甲、等级、职业、耐久、出售与到期条件的识别文本。 |
| `AttributePriority` | 列表 | 内置属性事件执行顺序；只在理解相互影响时调整。 |
| `ConditionPriority` | 列表 | 物品条件检查顺序。 |

最大生命的写入与客户端显示选项位于 `Feature/Attribute/definitions/builtin.yml` 的 `Attributes.Health` 节点：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `Health.Mode` | `HEALTH_SCALED` | `HEALTH_SCALED` 使用 3.9.2 的 base value 写入并允许压缩血条；`ATTRIBUTE_MODIFIER` 使用 beta.6 的固定修饰器写入、保留外部 base，且关闭客户端血条压缩。 |
| `HealthScaled.Enabled` | `true` | 仅在 `HEALTH_SCALED` 模式下控制是否启用 Bukkit 客户端生命缩放。 |
| `HealthScaled.Value` | `40` | 启用缩放时显示的生命上限，`40` 对应两排红心。 |

### 玩家背包饰品槽位

`RegisterSlots.Enabled: true` 时，默认配置将玩家背包三排最右侧设为饰品位置：第一排 `17#宝藏`、第二排 `26#戒指`、第三排 `35#项链`。`0..8` 是快捷栏，`9..35` 是背包三排，均为 Bukkit 的玩家背包编号。

玩家可执行 `/sx displaySlot` 查看位置，然后按 E 在自己的背包中放入或取出饰品。物品 Lore 必须包含对应识别名，例如槽位 26 中的物品须含“戒指”。点击、拖拽、Shift 移动、数字键换位后会在下一 tick 刷新属性；打开箱子时，下方玩家背包中的相同位置也生效。

启用 RPGInventory 联动时，装备扫描沿用 `RPGInventorySlot` 配置，`displaySlot` 会提示使用 RPGInventory 装备栏。

### NBT 属性节点

`NBTAttribute.Nodes` 使用展开式列表配置。每个路径由 SX-Item 的 NBT Wrapper 按点号逐级读取，节点值可以是单条属性文本、属性文本列表，或以属性名为键的嵌套映射：

```yml
NBTAttribute:
  Nodes:
    - 'SX-Attribute.Feature.Affix.Attributes'
    - 'SX-Attribute.Feature.Enhance.Attributes'
    - 'OtherPlugin.Attributes'
```

装备拓展会把未加显示标记的计算结果写入 `SX-Attribute.Feature.<模块>.Attributes`，默认配置已列出全部内置模块。Lore 或 SX-Item Lock 中的显示副本带有 `§X` 前缀，解析器会将其截断，因此同一属性不会因显示文本重复累计。

### 装备拓展显示模式

`EquipmentFeature.LoreMode` 只控制显示权，不改变 `State` 和 `Attributes` NBT：

- `LORE`：SX-Attribute 通过 `ItemMeta` 直接维护模块显示行，并使用 `.Rendered` NBT 精确移除上一版文本。
- `VARIABLE`：SX-Attribute 不直接安排 Lore 位置，只向 SX-Item 提供 `<l:SXAttribute_<模块>_Lore>` 锁变量。SX-Item 模板引用变量时才显示对应内容。

模式名称不区分大小写。无效值会回退到 `VARIABLE` 并输出警告；修改后执行 `/sxa reload` 即可生效。

## 消息与面板

`Message.yml` 管理战斗提示、出售/修理 GUI、属性面板和指令文本。颜色使用 `&`；文本内 `{0}`、`{1}` 等为运行时参数。开启 `Feature/Attribute/Attributes.yml` 的 `Settings.AutoPanel` 后，属性面板优先按各定义的 `Display` 自动生成，旧 `INVENTORY.STATS.*_LORE` 仍可作为回退。

## 配置重载边界

`/sxa reload` 会重载大多数 YAML、属性分片、来源规则和装备模块，并安全重建在线玩家属性。以下情况必须完整重启：

- 安装或移除 SX-Item、PacketEvents、ProtocolLib、Vault 等依赖插件。
- 修改 `Compatibility.MythicMobs` 等启动期兼容设置。
- 新增或删除需要服务端真实注册的自定义附魔 ID。
- 变更 JavaScript 引擎运行环境。

已在启动时注册粒子包适配器时，`DamageParticleLimit` 的数值调整可通过 `/sxa reload` 生效；若插件启动时该值为 `-1`，之后要启用粒子限流则需要完整重启。
