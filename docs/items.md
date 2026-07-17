# 物品配置与随机表达式

物品模板位于插件数据目录的 `Item/`。启动和 `/sxa reload` 时会递归读取其中的 YAML；`Item/NoLoad/` 是唯一排除目录，适合存放示例、备份或暂不启用的模板。

物品 ID 在全部已加载文件中必须唯一。重复 ID 会记录警告，并保留先加载的模板。

## 模板类型

### `SX`：配置生成

`SX` 是默认类型。它根据以下字段生成 `ItemStack`，并在生成时处理 SX-Item 表达式和随机字符串：

```yml
FlameBlade:
  Type: SX
  Name: "&c炎之剑"
  ID: DIAMOND_SWORD
  Lore:
    - "&6物品类型: 主武器"
    - "&c攻击力: +20"
    - "&6限制等级: 10级"
  EnchantList:
    - DAMAGE_ALL:3
  ItemFlagList:
    - HIDE_ATTRIBUTES
  Unbreakable: false
  Update: true
  ClearAttribute: true
```

| 字段 | 说明 |
|---|---|
| `Name` | 显示名称，可使用颜色代码与表达式。 |
| `ID` | 材质名或旧版 `数字ID:耐久值`；也可写成列表以随机选择一个 ID。 |
| `Lore` | Lore 行列表，属性与条件通常由此识别。 |
| `EnchantList` | 原版附魔列表，格式为 `附魔名:等级`。 |
| `ItemFlagList` | `ItemFlag` 枚举名列表，例如 `HIDE_ATTRIBUTES`。 |
| `Unbreakable` | 是否设置为不可破坏。 |
| `Color` | 皮革装备颜色，使用十六进制 RGB，如 `ffffff`。 |
| `SkullName` | 头颅拥有者名称。 |
| `Update` | 为模板物品写入模板 ID 与内容哈希；内容变更后可由自动更新流程刷新。 |
| `ClearAttribute` | 全局 `ClearDefaultAttribute` 开启时，是否清除该装备的原版属性修饰符，默认 `true`。 |
| `AttackSpeed` | 物品默认攻速配置；具体攻速规则由 `AttackSpeed.yml` 控制。 |

`Update: true` 只更新由该模板生成且哈希已变化的物品。`Type: Import` 不支持自动更新。

### `Import`：保存完整物品

`Import` 将 Bukkit 序列化后的完整 `ItemStack` 保存到 `Item` 字段，适合保留复杂 NBT、药水、书本或服务端附加数据：

```yml
SavedItem:
  Type: Import
  Item:
    ==: org.bukkit.inventory.ItemStack
    type: APPLE
```

手持物品时可使用 `/sxa save <编号> <Type>` 保存模板，例如 `/sxa save DemoSword SX` 或 `/sxa save SavedItem Import`。保存结果位于 `Item/Type-<Type>/Item.yml`；使用 `/sxa give <玩家> <编号> [数量]` 发放，并可用 `/sxa nbt` 查看手持物品的 NBT。

## 随机字符串与 SX-Item 表达式

`RandomString/` 下的 YAML 会递归加载为随机组。每个顶级键对应一个字符串或字符串列表；列表项等概率抽取。不存在的随机组会使所在 Lore 行被删除。

`SX` 模板使用 SX-Item 的表达式处理器。最常用格式如下：

| 格式 | 用途 |
|---|---|
| `<s:组名>` | 从 `RandomString` 中抽取一项；也可用 `<s:A:B:C>` 在内联选项中抽取。 |
| `<l:键>` | 锁定随机值。同一件物品内相同键会得到相同结果，并写入 NBT 供后续 Lore 重建使用。 |
| `<r:min_max>`、`<i:min_max>` | 随机整数。 |
| `<d:min_max>` | 随机小数。 |
| `<c:表达式>` | 数值计算；`<c:int 表达式>` 会取整。 |
| `<t:时长>` | 写入格式化的到期时间，例如 `<t:600>`。 |
| `<b:名称:匹配值...>` | 条件删除整行：当前值不匹配时删除该 Lore 行。 |

随机组可以递归引用，例如 `<s:<l:品质>Color>` 会先锁定品质，再读取对应的颜色组。生成器会把锁定结果写入物品 NBT，因此同一物品不会因重建 Lore 而改变已锁定的随机结果。

完整表达式能力由所安装的 SX-Item 版本提供；如果模板使用了脚本表达式或其它 SX-Item 扩展，请同步查阅对应版本的 SX-Item 文档。

## 条件 Lore

条件管理器按照 `Config.yml` 的 `Condition.Priority` 顺序解析 Lore。内置条件包括装备位置、最低等级、职业限制、Lore 耐久、出售价格和到期时间；各条件的显示关键词同样由 `Config.yml.Condition` 配置决定。

常见示例：

```yml
Lore:
  - "&6物品类型: 主武器"
  - "&6限制等级: 30级"
  - "&6限制职业: 战士"
  - "&7耐久度: 120/120"
  - "&e出售价格: 250"
  - "&a到期时间: 2026-12-31 23:59:59"
```

条件不满足时，物品不会参与对应装备位置的属性计算。自定义条件需要在 SX-Attribute 启用前注册，并在 `Condition.Priority` 中显式启用；开发接入方式见 [外部属性注册指南](api/external-attributes.md)。
