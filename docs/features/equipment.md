# 锻造与装备成长

所有装备模块由 `/sxa forge` 统一入口调用，也可用单独模块 ID 打开。每个模块在 `Feature/<模块>/` 内包含 `Config.yml`、`Gui.yml`、`Messages.yml`，以 `Enable` 单独开关。真实状态保存为 `SX-Attribute.Feature.<模块>.State` NBT，计算出的属性文本保存为同命名空间下的 `.Attributes`。

## 统一规则

- 成本统一支持 Vault 金币、总经验、等级及带 NBT 条件的物品。
- 强化与升星失败策略支持 `KEEP`、`DOWNGRADE`、`RESET`、`DESTROY`；保护概率可用公式配置。
- GUI 对玩家会话加锁，并以主手物品指纹阻止换物、重复点击和异常扣费。
- 关闭模块不会删除已有 NBT；重新开启后可继续读取并渲染状态。
- Lore 只是显示副本，统一带 `§X` 标记，不直接参与属性累计。

## 属性 NBT 与显示模式

每个模块使用三个互相隔离的节点：

| 节点 | 职责 |
|---|---|
| `SX-Attribute.Feature.<模块>.State` | 唯一状态事实源，例如等级、词缀 ID、随机值和孔位内容。 |
| `SX-Attribute.Feature.<模块>.Attributes` | 未加显示标记的属性文本，由 `NBTAttribute.Nodes` 读取并参与计算。 |
| `SX-Attribute.Feature.<模块>.Rendered` | `LORE` 模式记录上一次直接渲染的行，只用于精确移除和旧物品迁移。 |

`Config.yml` 的 `EquipmentFeature.LoreMode` 提供两种互斥模式：

```yml
EquipmentFeature:
  # LORE / VARIABLE
  LoreMode: VARIABLE
```

### `LORE` 模式

SX-Attribute 从 `State` 生成显示文本，添加 `§X` 后直接写入物品 Lore，并将本次结果记录到 `.Rendered`。重新渲染时只移除 `.Rendered` 中的旧行，不影响 SX-Item 模板或其它插件添加的 Lore。

### `VARIABLE` 模式

SX-Attribute 将显示块写入 SX-Item Lock NBT，变量名为 `<l:SXAttribute_<模块>_Lore>`。SX-Item 物品模板决定变量的位置，也可以完全不引用：

```yml
Lore:
  - '<l:SXAttribute_Quality_Lore>'
  - '<l:SXAttribute_Affix_Lore>'
  - '<l:SXAttribute_Enhance_Lore>'
```

模块没有显示内容时，变量使用 SX-Item 删行协议，不产生空白 Lore。装备状态变化后 SX-Attribute 请求 SX-Item 按模板更新物品；`SXItemUpdateEvent` 中只迁移 `State` 和派生 NBT，不直接修改变量模式的 Lore。

从 `LORE` 切换到 `VARIABLE` 时，检测到旧 `.Rendered` 会触发一次 SX-Item 模板重建；从 `VARIABLE` 切回 `LORE` 时，旧 Lock 变量会先置为删行值，再写入直接显示行，避免两套文本同时存在。

## 物品生成与属性装载生命周期

装备从模板生成到最终提供角色属性，会依次经过以下阶段：

1. **生成基础物品**：SX-Item 解析物品模板中的名称、Lore、表达式和 `<l:...>` 变量，把锁定值写入 `SX-Item.Lock`，再写入物品 ID 与模板哈希并触发 SX-Item 的 `SXItemSpawnEvent`。此时尚未生成过的装备模块变量使用 SX-Item 删行协议，不会留下空白 Lore。
2. **修改模块状态**：品质、词缀、强化等操作读取 `SX-Attribute.Feature.<模块>.State`，执行模块规则后将新状态写回。`State` 是唯一状态事实源，不能从 Lore 反推等级、随机值或词缀身份。
3. **派生属性文本**：模块根据最新 `State` 计算未加显示标记的属性行，写入 `SX-Attribute.Feature.<模块>.Attributes`。该节点是装备拓展属性参与计算的入口。
4. **同步显示副本**：`LORE` 模式直接维护带 `§X` 标记的 Lore，并用 `.Rendered` 记录本次写入内容；`VARIABLE` 模式把同样带标记的多行文本写入 `SX-Item.Lock.SXAttribute_<模块>_Lore`，显示位置由 SX-Item 模板中的 `<l:SXAttribute_<模块>_Lore>` 决定。
5. **按需重建 SX-Item**：`VARIABLE` 模式的状态操作会请求 SX-Item 更新物品。SX-Item 用当前模板生成新物品，继承旧 Lock 值和受保护数据，触发 `SXItemUpdateEvent`，然后把新类型和物品元数据应用到原物品。
6. **迁移装备拓展状态**：SX-Attribute 监听 SX-Item 的更新事件，将每个模块的 `State` 从旧物品复制到新物品，再重新派生 `.Attributes` 和显示数据。事件内不会再次请求 SX-Item 更新，避免递归重建。
7. **收集已装备物品**：玩家属性刷新时，依次收集 RPGInventory 槽位、自定义注册槽位、盔甲、主手和副手。SX-Attribute 旧 `Item/` 模板物品会先执行自己的哈希更新流程；该流程与 SX-Item 的同名生成事件属于不同事件类型。
8. **预加载并刷新派生数据**：系统先读取当前 Lore/NBT 供装备条件判断，触发 `SXPreLoadItemEvent`，随后装备模块在事件最低优先级重新生成 `.Attributes` 和显示副本。完成刷新后会再次读取物品，保证本轮计算使用最新数据。
9. **解析并合并属性**：解析器合并 Lore 与 `NBTAttribute.Nodes` 配置的节点值。每行遇到 `§X` 即截断，因此显示副本不会重复计入；未标记的 `.Attributes` 会正常进入旧属性解析器和动态属性引擎。
10. **结算最终物品来源**：各装备先按来源分组并触发 `SXLoadAttributeEvent`，套装等跨物品模块在这里按全部已装备物品聚合。最终只替换 SX-Attribute 管理的装备来源，外部插件写入的其它命名来源不会被清除。

生命周期中各数据的职责始终不变：`State` 保存事实，`Attributes` 提供实际数值，Lore 和 SX-Item Lock 只提供显示。`VARIABLE` 仅适用于能被 SX-Item 识别并重建的物品；普通 Bukkit 物品和 SX-Attribute 旧 `Item/` 模板物品需要自动显示时应使用 `LORE`。

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
