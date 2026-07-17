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

一个动态属性由 `Values`、`Triggers`、`Display` 组成。以下示例为火元素伤害：

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

## 显示与重载

`Display.Category`、`Order`、`Rows` 控制属性面板；全局 `Settings.AutoPanel` 开启后会自动生成统计面板行。修改普通属性分片后执行 `/sxa reload`，新注册表会先校验，再原子替换并重算在线实体。
