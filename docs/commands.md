# 指令、权限与 Placeholder

主命令为 `/sxAttribute`，别名为 `/sx`、`/sa`、`/sxa`。不带参数会显示当前发送者可用的子命令。

## 常用指令

| 指令 | 说明 |
|---|---|
| `/sxa stats [玩家]` | 打开属性面板；管理员可查看在线玩家。 |
| `/sxa forge [模块]` | 打开锻造 GUI；可选择 `SetBonus`、`Quality`、`Affix`、`Enhance`、`Star`、`Reforge`、`Reroll`、`Socket`、`EnchantGrowth`。 |
| `/sxa reload` | 重载插件配置、属性分片、来源和装备模块。 |
| `/sxa source [玩家]` | 查看属性来源与贡献。 |
| `/sxa statssource [玩家]` | 打开属性来源统计面板。 |
| `/sxa update [玩家]` | 重新读取装备来源并刷新 UPDATE 属性。 |
| `/sxa persistent <玩家> <源名> <属性词条...>` | 添加旧兼容持久化来源。 |
| `/sxa del-persistent <玩家> <源名>` | 删除旧兼容持久化来源。 |
| `/sxa attributelist` / `conditionlist` | 列出当前已加载属性/条件。 |
| `/sxa nbt` | 查看主手物品 NBT，排查装备模块状态。 |
| `/sxa give` / `save` | SX-Item 物品的管理与保存操作。 |
| `/sxa sell` / `repair` | Vault 可用时打开出售/修理界面。 |

具体参数、可见性和管理权限以游戏内 `/sxa` 帮助为准；不同可选依赖会使对应子命令不可用。

## 权限

| 权限 | 默认 | 作用 |
|---|---|---|
| `sx-attribute.use` | 所有人 | 基础插件使用权限。 |
| `sx-attribute.stats` | 所有人 | 查看自身属性面板。 |
| `sx-attribute.forge` | OP | 使用配置化锻造界面。 |
| `SX-Attribute.admin` | OP 检查 | 查看他人属性与大部分管理子命令。 |

## Placeholder

安装 PlaceholderAPI 后，可使用 `%sx_<属性占位符>%`。例如 `%sx_Damage%`、`%sx_CritRate%`、`%sx_Health%`。动态属性字段以 `%sx_<属性ID>_<字段>%` 读取，例如 `%sx_FireDamage_damage%`。

可用占位符以 `/sxa attributelist` 与各属性 `Display.Rows.placeholder` 为准。属性值为零时，自动面板可按配置隐藏对应 Lore 行。
