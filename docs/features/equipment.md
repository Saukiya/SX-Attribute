# 锻造与装备成长

所有装备模块由 `/sxa forge` 统一入口调用，也可用单独模块 ID 打开。每个模块在 `Feature/<模块>/` 内包含 `Config.yml`、`Gui.yml`、`Messages.yml`，以 `Enable` 单独开关。真实状态保存为 `SX-Attribute.Feature.<模块>.State` NBT，Lore 会在物品加载和操作后重建。

## 统一规则

- 成本统一支持 Vault 金币、总经验、等级及带 NBT 条件的物品。
- 强化与升星失败策略支持 `KEEP`、`DOWNGRADE`、`RESET`、`DESTROY`；保护概率可用公式配置。
- GUI 对玩家会话加锁，并以主手物品指纹阻止换物、重复点击和异常扣费。
- 关闭模块不会删除已有 NBT；重新开启后可继续读取并渲染状态。

成本节点的实际字段如下；扣除前会先完整校验。仅在已经扣除后发生异常时才回滚，材料返还背包，溢出则掉落在玩家位置：

```yml
Cost:
  Money: 200
  Experience: 0
  Levels: 0
  Items:
    - Material: IRON_INGOT
      Amount: 1
      NbtKey: sx.material       # 可选
      NbtValue: enhance_stone   # 可选；省略时只要求存在 NbtKey
```

## 模块索引

| 模块 | 配置目录 | 作用与关键配置 |
|---|---|---|
| 套装 `SetBonus` | `Feature/SetBonus/` | `Sets.<ID>.Thresholds` 配置两件、四件或任意阈值；`Mixes` 配置多个套装的混搭要求。 |
| 品质 `Quality` | `Feature/Quality/` | `Qualities.<ID>.Weight` 决定权重，`Attributes` 写入品质属性文本。 |
| 词缀 `Affix` | `Feature/Affix/` | `Count` 或 `Formula.Count` 决定条数；每个词缀配置 `Weight`、`Rare`、`Min/Max` 与 Lore 模板。 |
| 强化 `Enhance` | `Feature/Enhance/` | `MaxLevel`、`Formula.SuccessChance`、`Failure` 和 `Attributes` 控制等级成长。 |
| 升星 `Star` | `Feature/Star/` | 与强化独立，使用自己的等级上限、成功率、成本和失败规则。 |
| 重铸 `Reforge` | `Feature/Reforge/` | 更换词缀身份；状态中的 `LockedSlots` 可保留指定词缀槽位。 |
| 洗练 `Reroll` | `Feature/Reroll/` | 保留词缀身份，仅按该身份的 `Min/Max` 重掷数值。 |
| 宝石孔 `Socket` | `Feature/Socket/` | `DefaultSlots`、`Gems`、`Extraction` 控制孔位、镶嵌和拆卸损毁概率。 |
| 附魔成长 `EnchantGrowth` | `Feature/EnchantGrowth/` | 原版或自定义附魔的经验、等级、属性与 Lore 降级。 |

## 套装与品质

套装状态只保存 `SetId`。装备加载时会统计穿戴/注册槽位内同套装件数，满足 `Thresholds.2`、`Thresholds.4` 等节点后将对应 `Attributes` 加入角色属性。`Mixes.<ID>.Requirements` 的每一项都满足时，额外添加混搭属性。

品质在操作时从 `Qualities` 按 `Weight` 抽取，品质名称和 `Attributes` 与物品状态分离，修改配置后可重建 Lore。

## 词缀、重铸与洗练

词缀状态为 `ID|数值`，不会把已掷属性写死在 Lore。`Affix` 首次生成身份和数值；`Reforge` 重新抽身份并保留状态中的 `LockedSlots`；`Reroll` 仅重新掷数值。`Rare` 是供 GUI、Lore 或外部扩展识别的稀有标记，不会隐式改变概率，实际概率始终由 `Weight` 决定。

## 强化与升星

两者都使用独立状态和独立成本。`Formula.SuccessChance` 接收 `level` 等公式变量；`Failure.Policy` 控制失败结果，`Failure.Levels` 作为降级层数，`ProtectionChance` 为保护触发率。`Attributes.<ID>.Formula` 将当前等级换算成附加属性文本。

## 宝石孔

正常点击时，从副手读取带有 `SocketGem` NBT 的宝石并消耗一个；潜行点击时拆卸最后一个宝石。拆卸受 `Extraction.DestroyChance` 控制，未损毁的宝石会返还背包，溢出时掉落在玩家位置。每个 `Gems.<ID>` 可指定材质和属性文本。

## 附魔成长

`Gain` 分别配置伤害、击杀、采集和钓鱼获得的附魔经验。`RequiredExperience` 使用公式计算每级需求，升级会触发 `SXEnchantGrowthEvent`。原版附魔通过 `VanillaName` 写入 Bukkit；自定义附魔优先接管真实注册表，失败时：

- `Fallback: LORE`：仍写入 NBT、经验、等级、属性和 Lore，但不写 Bukkit 附魔与光效。
- `Fallback: DISABLE`：拒绝应用该自定义附魔。

如需让锻造按钮默认使用自定义附魔，将 `DefaultEnchant` 改为该 `Enchants` 节点 ID。
