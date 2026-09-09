# 统一属性引擎

属性引擎把元素、克制、特效、Buff、Debuff 和条件统一表示为“属性值 + 触发器 + 条件 + 动作”。不需要为每一种玩法新增 Java 监听器。

## 文件结构

```text
Feature/Attribute/
├── Attributes.yml              # 聚合清单、加载顺序与全局开关
└── definitions/
    ├── builtin.yml             # 内置兼容属性
    └── custom.yml              # 元素、克制、状态等自定义示例
```

`Attributes.yml` 的 `Files` 决定分片加载顺序。`DuplicatePolicy: ERROR` 时，重复属性 ID 会拒绝新注册表并保留当前可用注册表。

## 属性定义

### 原版属性的版本限制

带 `RegistryKey` 的属性依赖服务端原版能力，同时受 `Version` 最低版本和实际 Bukkit API 是否存在的约束。
Minecraft 1.12.2 没有以下采集属性，升级 PlaceholderAPI 或调低 `Version` 不会使它们生效：

| 属性 | 最低 Minecraft 版本 |
| --- | --- |
| 交互距离 `BlockRange`、挖掘速度 `BlockBreakSpeed` | 1.20.5 |
| 挖掘效率 `MiningEfficiency`、水下挖掘速度 `SubmergedMining`、氧气加成 `OxygenBonus` | 1.21 |

`/sxa stats` 自动面板会跳过未启用或不受支持的属性；关闭 `Settings.AutoPanel` 后，旧 `Message.yml` 中
引用这些属性的行也会隐藏。点击“显示更多属性”只显示可用属性的零值，不会显示旧服不支持的能力。
自定义行混合多个属性时，若其中一个已知属性不可用则隐藏整行；未知占位符仍保留，便于排查拼写或扩展加载问题。
此处修复的是 issue #55 中的面板 `N/A` 显示；若插件本身无法启用，仍需完整启动日志定位，不能据此判断为 PAPI 故障。

### 动态属性

一个动态属性由 `Values`、可选的 `Mappings`、`Triggers`、`Display` 组成。以下示例为火元素伤害：

```yml
Attributes:
  FireDamage:
    Enable: true
    Priority: 500
    Values:
      damage: { Match: 火元素伤害, MatchMode: CONTAINS, Aggregate: SUM, Min: 0, Max: 100000 }
      chance: { Match: 火元素触发, MatchMode: CONTAINS, Aggregate: SUM, Min: 0, Max: 100 }
    Triggers:
      - Event: DAMAGE_ATTACK
        When: "<c:<l:self_damage> > 0 && <l:self_chance> > 0>"
        Actions:
          - Type: DAMAGE
            Mode: ADD
            Chance: "<c:<l:self_chance>>"
            Formula: "<c:<l:self_damage> * (100 - <l:defender_FireResistance_resistance>) / 100>"
```

`Match` 指向 Lore 中的识别文本；`MatchMode` 可为 `CONTAINS`、`PREFIX`、`EQUALS` 或 `REGEX`，`Aggregate` 可为 `SUM`、`MAX`、`MIN`、`LAST`。值会经过 `Min`/`Max` 修正后写入动态字段，可由 API、Placeholder 和其它定义读取。

## 自定义属性映射

`Mappings` 可把当前定义中的字段按比例派生为任意可识别属性。`Target` 填目标属性的 Lore 识别名，因此既支持内置的攻击力、暴击几率，也支持其它用户自定义属性：

```yml
# 所有自定义属性定义都必须放在 Attributes 根节点下。
Attributes:
  # Strength 是属性 ID；公式跨属性引用时使用这个 ID，例如 Strength_strength。
  Strength:
    # 是否注册力量属性；关闭后 Values、Mappings 与 Display 均不生效。
    Enable: true
    # 属性定义优先级；数值越小，相关触发器越早执行。
    Priority: 300
    # Values 声明该属性拥有的全部可解析字段，也是映射公式可引用的变量来源。
    Values:
      # strength 是主字段 ID；Mapping.Source 使用此名称，公式中可用 value 或 source_strength 读取。
      strength:
        # 从 Lore、NBT 或属性来源中识别“力量: 数值”。
        Match: 力量
        # CONTAINS 表示属性文本中包含 Match 即可识别。
        MatchMode: CONTAINS
        # SUM 表示不同装备和属性来源提供的力量相加。
        Aggregate: SUM
        # 力量最终修正时允许的最小值。
        Min: 0
        # 力量最终修正时允许的最大值。
        Max: 100000
        # 力量本身不额外计算战斗力；映射目标仍按各自规则计算。
        CombatPower: 0
      # attack_bonus 是同一定义的辅助字段，用来演示 Formula 读取属性源中的其它属性。
      attack_bonus:
        # 从属性文本中识别“攻击映射倍率: 数值”。
        Match: 攻击映射倍率
        # 使用 CONTAINS 兼容带颜色、前缀或后缀的属性文本。
        MatchMode: CONTAINS
        # 多个来源提供的攻击映射倍率累加后再进入 Scale 公式。
        Aggregate: SUM
        # 不允许辅助倍率低于 0。
        Min: 0
        # 最多允许额外增加 1000% 映射倍率。
        Max: 1000
        # 辅助字段只参与映射公式，不单独增加战斗力。
        CombatPower: 0
    # Mappings 将聚合后的自定义字段转换成内置属性或其它自定义属性。
    # Scale 只计算倍率，即“每 1 点 Source 对应多少点 Target”。
    # Formula 计算最终写入 Target 的数值；不填写时默认结果为 value * scale。
    # Formula 填写后可使用 value、scale 与其它属性变量，实现非线性或跨属性计算。
    Mappings:
      # 第一条映射把力量转换为攻击力，并演示 Scale 与 Formula 的完整变量用法。
      - # Source 指定读取 Values.strength；其总值同时注入变量 value。
        Source: strength
        # Target 使用目标属性的 Lore 识别名，因此这里会进入内置“攻击力”计算。
        Target: 攻击力
        # Scale 只计算倍率；这里表示每点力量基础映射 2 点攻击力，再叠加 attack_bonus/100。
        Scale: "<c:2 + <l:source_attack_bonus> / 100>"
        # Formula 计算最终值；这里显式写出的 value * scale 与省略 Formula 时的默认行为相同。
        Formula: "<c:<l:value> * <l:scale>>"
      # 第二条映射把力量转换为暴击几率，演示固定倍率也通过公式引擎执行。
      - # Source 仍读取同一个 strength 字段，允许一个源字段映射到多个目标。
        Source: strength
        # 目标“暴击几率”的数值单位为百分比。
        Target: 暴击几率
        # Scale 固定返回倍率 2，表示每 1 点力量对应 2% 暴击几率。
        Scale: "<c:2>"
        # Formula 是最终结果；此处等于力量总值乘倍率 2，也可以直接省略这一行。
        Formula: "<c:<l:value> * <l:scale>>"
    # Display 控制 /stats 自动属性面板中的展示位置。
    Display:
      # OTHER 表示显示在其它属性分类。
      Category: OTHER
      # 同分类中按 Order 从小到大排列。
      Order: 1
      # Rows 声明该属性需要展示的面板行。
      Rows:
        # placeholder 使用“属性ID_字段ID”读取力量总值。
        - placeholder: Strength_strength
          # 面板中显示的中文名称。
          label: 力量
          # 面板文字使用金色。
          color: "&6"
        # 同时展示用于攻击力公式的辅助倍率字段。
        - placeholder: Strength_attack_bonus
          # 面板中显示的辅助字段名称。
          label: 攻击映射倍率
          # 辅助倍率使用黄色。
          color: "&e"
          # 该字段按百分比展示。
          suffix: "%"
```

只有 `力量: 1` 时，以上配置增加 `2` 点攻击力和 `2%` 暴击几率；同时拥有 `攻击映射倍率: 50` 时，攻击力的 `Scale` 为 `2 + 50 / 100 = 2.5`，因此每点力量增加 `2.5` 攻击力，暴击几率仍为每点 `2%`。映射在玩家全部来源聚合后统一计算，不会因力量分散在几件装备上而改变结果。所有映射读取同一份原始字段快照，因此不会级联执行；A 映射到 B、B 再映射到 C 时，C 不会获得 A 的间接加成。

完整计算示例：玩家拥有 `力量: 10` 与 `攻击映射倍率: 50` 时，攻击力映射先计算 `Scale = 2 + 50 / 100 = 2.5`，再执行 `Formula = 10 * 2.5 = 25`；暴击映射计算 `Scale = 2`，再执行 `Formula = 10 * 2 = 20`，最终增加 `25` 点攻击力和 `20%` 暴击几率。

`Scale` 也通过 SX-Item 公式引擎求值，结果作为倍率与源字段相乘；可写固定公式如 `"<c:2>"`，也可以引入以下变量：

- `<l:value>`：当前 `Mapping.Source` 指定字段的总值。
- `<l:scale>`：当前 Mapping 的 `Scale` 公式完成求值后的倍率，仅在最终 `Formula` 中使用。
- `<l:字段>`、`<l:source_字段>`、`<l:self_字段>`：当前属性定义中的任意字段；三种写法数值相同。
- `<l:属性ID_字段>`：同一属性快照中其它内置或自定义属性的字段，例如 `<l:Crit_CritRate>`。

例如 `Scale: "<c:2 + <l:source_bonus> / 100>"` 可以让当前源字段以基础 `2` 倍加上同定义 `bonus` 字段提供的倍率。`Formula` 可覆盖最终线性结果，并共享上述全部变量。所有变量均来自映射开始前的原始属性快照，所以不同映射不会互相污染或形成隐式级联。公式引擎不可用或求值失败时，纯数字 `Scale` 使用自身数值回退，公式型 `Scale` 回退为 `1`。

### Scale 与 Formula 的区别

`Scale` 回答“每 1 点源属性对应多少点目标属性”，其结果是倍率；`Formula` 回答“最终应向目标属性写入多少数值”，其结果是最终映射值。执行顺序固定如下：

1. 从 `Source` 读取源字段总值并写入变量 `value`。
2. 通过公式引擎计算 `Scale`，并把结果写入变量 `scale`。
3. 计算默认结果 `value * scale`。
4. 若配置了 `Formula`，通过公式引擎计算最终结果；未配置或求值失败时使用上一步默认结果。

因此以下两种配置完全等价：

```yml
# 简单线性映射只需要 Scale；最终结果自动使用 value * scale。
Scale: "<c:2>"
```

```yml
# Formula 显式写出默认线性计算，结果仍是 value * scale。
Scale: "<c:2>"
Formula: "<c:<l:value> * <l:scale>>"
```

只有需要非线性计算、固定最终值或组合其它属性时才必须填写 `Formula`，例如：

```yml
# Scale 先计算基础倍率 2。
Scale: "<c:2>"
# Formula 在默认线性结果上额外加入当前暴击几率的 10%。
Formula: "<c:<l:value> * <l:scale> + <l:Crit_CritRate> / 10>"
```

## 触发器与变量

全部事件为：

| 触发器 | 对应时机 |
|---|---|
| `LOAD` / `EQUIP` / `UNEQUIP` | 装备属性加载、装备快照改变后首次装备、替换前旧快照卸下。 |
| `DAMAGE_ATTACK` / `DAMAGE_DEFEND` / `DAMAGE_AFTER` | SX 伤害处理的攻击侧、受击侧和伤害结算后阶段。 |
| `PROJECTILE_SHOOT` | `EntityShootBowEvent` 发生时。 |
| `HEAL` | `EntityRegainHealthEvent` 未取消时。 |
| `EXP_GAIN` | `PlayerExpChangeEvent` 未取消时；额外提供 `event_exp`。 |
| `KILL` / `DEATH` | `EntityDeathEvent`；存在击杀者时会额外触发 `KILL`。 |
| `TICK` | 对每个在线玩家按 `Settings.TickPeriod` 触发。 |
| `API` | 外部调用 `SXAPI.triggerDynamicAttributes` 时。 |

公式可读取以下变量：

- `self_<字段>`：当前定义、当前触发侧的字段值。
- `attacker_<属性ID>_<字段>` 与 `defender_<属性ID>_<字段>`：双方动态属性。
- `attacker_health`、`defender_health`、`*_max_health`、`distance`、`event_damage`、`event_pvp`、`event_crit`、`event_cancelled`。
- `defender_type_SKELETON` 等实体类型标记，命中时为 `1`，否则为 `0`。
- `attacker_sneaking`、`defender_sneaking`、`world_time`、`world_storm`。
- `attacker_tag_<标签>`、`defender_tag_<标签>`，以及 `<前缀>_source_<安全来源名>_stacks` 与 `<前缀>_source_<安全来源名>_remaining`。剩余时间以 tick 计，永不过期为 `-1`；来源名中的非字母、数字、下划线会转换为下划线。

例如亡灵克制可在 `Chance` 中写入 `defender_type_ZOMBIE + defender_type_SKELETON`；这不会修改或取消其它生物的伤害事件。

## 动作 DSL

`Actions` 仅允许安全原语：`DAMAGE`、`HEAL`、`CANCEL`、`VANILLA_ATTRIBUTE`、`POTION`、`SOURCE_APPLY`、`SOURCE_REMOVE`、`FIRE`、`LIGHTNING`、`COMMAND`、`MESSAGE`、`HOLOGRAM`、`PARTICLE`、`SOUND`、`EXP`、`DURABILITY`。

`DAMAGE` 的 `Mode` 为 `ADD`（默认，额外伤害）、`SET`（设为公式结果）或 `MULTIPLY`（以公式结果乘现有伤害）。`SOURCE_APPLY` 使用 `Source`、`Attributes`、`Duration`、`MaxStacks`、`StackMode`、`Persistent` 与 `Tags`；`POTION` 使用 `Potion`、`Duration`、`Amplifier`；原版属性使用 `RegistryKey`、`LegacyName`、`Formula`。所有动作可配置 `Target: ATTACKER|DEFENDER` 和 `Chance`，未指定目标时会按当前触发侧选择对方。

`CANCEL` 会取消当前 SX 伤害事件，应始终配合明确的 `When` 或 `Chance`。对低血量 Buff，请同时判断属性值和血量，避免没有该词条的生物也获得来源。

### 元素反应与持续来源

元素关系可用标签和来源组合实现。以下配置让火伤命中带有水标签的目标时追加伤害，并施加最多 3 层的临时来源：

```yml
Attributes:
  Vaporize:
    # 反应条件显式依赖标签，避免隐式修改所有伤害事件。
    Enable: true
    Values:
      power: { Match: 蒸发强度, MatchMode: CONTAINS, Aggregate: SUM, Min: 0, Max: 1000 }
    Triggers:
      - Event: DAMAGE_ATTACK
        When: "<c:<l:self_power> > 0 && <l:defender_tag_water> > 0>"
        Actions:
          - Type: DAMAGE
            Target: DEFENDER
            Mode: ADD
            Formula: "<c:<l:self_power> * 1.5>"
          - Type: SOURCE_APPLY
            Target: DEFENDER
            Source: debuff:vaporize
            Duration: "<c:100>"
            MaxStacks: 3
            StackMode: ADD
            Persistent: false
            Tags: [vaporize]
```

`StackMode: ADD` 增加层数，`REFRESH` 只刷新时间；`SOURCE_REMOVE` 可在 `DEATH` 或 `UNEQUIP` 时清理临时来源。标签必须由规则明确写入。

## 显示与重载

`Display.Category`、`Order`、`Rows` 控制属性面板；全局 `Settings.AutoPanel` 开启后会自动生成统计面板行。修改普通属性分片后执行 `/sxa reload`，新注册表会先校验，再原子替换并重算在线实体。
