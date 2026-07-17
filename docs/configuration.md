# 主配置与安装说明

## 安装与依赖

将 SX-Attribute 与 **SX-Item** 放入 `plugins/` 后完整重启服务器。SX-Item 是必需依赖；Vault、PlaceholderAPI、HolographicDisplays、DecentHolograms、RPGInventory、MythicMobs 与 SkillAPI 为可选依赖。

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
| `ClearDefaultAttribute` | `true` | 是否清理物品默认原版属性修饰符，避免与 SX 数值重复。 |
| `RPGInventorySlot` | 列表 | RPGInventory 槽位编号。 |
| `RegisterSlots` | - | 自定义注册槽位。每项格式为 `槽位#名称`。 |
| `DefaultAttribute` | `生命上限: 20` | 实体没有物品属性时的基础属性 Lore。 |
| `Condition` | - | 主副手、护甲、等级、职业、耐久、出售与到期条件的识别文本。 |
| `AttributePriority` | 列表 | 内置属性事件执行顺序；只在理解相互影响时调整。 |
| `ConditionPriority` | 列表 | 物品条件检查顺序。 |

## 消息与面板

`Message.yml` 管理战斗提示、出售/修理 GUI、属性面板和指令文本。颜色使用 `&`；文本内 `{0}`、`{1}` 等为运行时参数。开启 `Feature/Attribute/Attributes.yml` 的 `Settings.AutoPanel` 后，属性面板优先按各定义的 `Display` 自动生成，旧 `INVENTORY.STATS.*_LORE` 仍可作为回退。

## 配置重载边界

`/sxa reload` 会重载大多数 YAML、属性分片、来源规则和装备模块，并安全重建在线玩家属性。以下情况必须完整重启：

- 安装或移除 SX-Item、Vault 等依赖插件。
- 修改 `Compatibility.MythicMobs` 等启动期兼容设置。
- 新增或删除需要服务端真实注册的自定义附魔 ID。
- 变更 JavaScript 引擎运行环境。
