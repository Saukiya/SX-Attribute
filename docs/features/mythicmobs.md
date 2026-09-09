# MythicMobs 属性技能

启用 `Config.yml` 的 `Compatibility.MythicMobs`，安装适合服务端版本的 MythicMobs。
SX 在 MM 加载技能事件中注册机制，首次安装需要重启；修改 MM 技能后使用 `/mm reload`。
修改 SX 属性定义后使用 `/sxa reload`。无需安装 AttributePlus 或 AttributeMM。

## 怪物出生自带属性

在 MM 的 **Mobs 怪物配置**中添加 `SXAttribute` 字符串列表，不需要给怪物另写出生技能。
SX 在未取消的 `MythicMobSpawnEvent` 中读取 MM 已加载的怪物配置，MM 4/5 使用相同写法。
完整示例见 [mythicmobs-mobs.yml](../examples/mythicmobs-mobs.yml)。

```yaml
SX_Warrior:
  Type: ZOMBIE
  Health: 200
  SXAttribute:
    - '攻击力=<c:20+<l:mob_level>*5>'
    - '防御力=<c:<l:普通基数>*10>'
    - '暴击几率: <r:10_20>%'
```

节点按 `SXAttribute` → `SX-Attribute` → `sxattribute` 优先读取，只采用第一个存在的节点；
`SXAttribute: []` 可显式关闭别名中的配置。每行支持 `属性识别名=公式`、完整 Lore、随机范围与多行随机词条，
识别范围与技能 `al` 相同，包含动态属性和脚本注册的识别器。SX 表达式需要开启 `Settings.FormulaEngine`；固定数值无需开启。

### `=` 与 `:` 的区别

这两个符号属于不同层级，不能混用：

| 写法 | 所在层级 | 含义 | 适用场景 |
|---|---|---|---|
| `参数=值` | MythicMobs 技能参数 | 给 MM 参数赋值；多个参数用 `;` 分隔。 | `a=20`、`dm=1.5`、`al=攻击力=100` |
| `属性名=公式` | SX 属性简写 | 右侧必须最终得到一个数字，再转换为单个属性值。 | `攻击力=20`、`攻击力=<c:10+<l:mob_level>*2>` |
| `属性名: Lore` | SX 完整 Lore | 整行交给 SX Lore 识别器，保留范围、百分比和多行结果。 | `攻击力: 10 - 20`、`暴击几率: 50%` |

例如技能参数：

```yaml
Skills:
  - 'SXDamage{a=20;dm=1.5;al=攻击力=100|暴击几率: 50%} @target'
```

解析时，最外层的 `a=20`、`dm=1.5` 和 `al=...` 是 MM 参数；`al` 的值内部仍由 SX 解析，
所以 `攻击力=100` 是 SX 简写，而 `暴击几率: 50%` 是完整 Lore。`al=攻击力=100` 中的第二个 `=`
不会把 MM 参数截断，兼容 MM 4.1 的旧参数解析器。

`=` 的右侧适合单个数字或数学公式，例如 `攻击力=<c:20+<l:mob_level>*5>`；
`:` 的右侧适合 SX 能识别的完整文本，例如范围 `攻击力: 10 - 20`、百分比 `暴击几率: 50%`、
随机范围 `攻击力: <r:10_20> - <r:30_40>` 和返回多行 Lore 的 `<s:词条组>`。
不要把范围写成 `攻击力=10 - 20`，这会被当作数学式而不是范围 Lore；也不要把 `属性名:`
单独写成 MM 参数，MM 参数仍必须使用 `参数=值`。包含 `:` 的整条 YAML 技能建议使用引号。

| 出生变量 | 含义 |
|---|---|
| `<l:mob_level>` | 出生事件的最终等级，保留小数，兼容旧 MM 的整数返回类型。 |
| `<l:mob_name_internal>` / `<l:mob_name_display>` | 怪物配置 ID / 实体显示名。 |
| `<l:mob_uuid>` | 本次怪物实体 UUID。 |
| `<l:mob_health>` / `<l:mob_max_health>` | 出生采样时的当前/最大生命。 |
| `<l:attacker_...>` / `<l:defender_...>` | 两端均代表怪物自身；属性数值来自已装备物品及其他已有来源，尚不包含本次出生配置。 |
| `<l:随机组名>` | 从 SX-Attribute/RandomString 抽取并锁定；同一怪物所有行共享结果，其他怪物独立。 |

也可使用 `<mob.level>`、`<caster.level>` 等出生别名，自身字段支持 `level`、`name`、`uuid`、
`health/hp`、`maxhealth/mhp`。出生没有技能目标与 trigger 上下文，不解析 `<target...>`、技能变量或任意 MM 占位符。
怪物通常不是玩家，因此没有玩家 PAPI 上下文；应优先使用上述 SX 出生变量。

属性写入固定独立来源 `mythic-mob:spawn`，不属于技能的 `mythic:` Buff 命名空间，不依赖受管来源功能是否启用。
装备重读不会覆盖该来源；同一实体重复收到出生通知时不叠加、不重抽随机结果。
全部配置行求值、解析及有限数值校验成功后才写入出生来源，错误日志会包含怪物配置 ID。
`SX-Equipment` 先装备，再读取装备属性供公式引用；映射统一在全部来源聚合时计算。

在实体所属线程收到出生事件时立即注册 SX 数值；Folia 的其他线程入口通过实体调度器执行，实体退役时取消。
UPDATE 类属性按 SX 现有机制在后续实体 tick 刷新。属性处理器原有的实体类型限制仍有效：
例如当前内置 `Health` 只写入玩家最大生命，怪物最大生命继续使用 MM 的 `Health` 配置。

修改配置并 `/mm reload` 后对**新出生怪物**生效，已有怪物保留本次出生结果。
出生来源跟随现有实体属性缓存清理（包括死亡），不会自动写入 SQL/Redis；重启后仅加载已有怪物而未发出生事件时不会自动重建。

## 版本兼容

共用逻辑使用 Java 8 与旧 Bukkit 实体/伤害 API。MM 4 采用 `SkillMechanic + boolean`，
MM 5 采用 `ISkillMechanic + SkillResult`，互不加载对方的类型。
MM 4.1 的旧变量解析器、整数等级和缺少 `ActiveMob.getDisplayName()` 的情况有独立处理；
MM 5 不依赖在 5.13 中被移除的旧 `SkillMechanic` 构造器。

这套机制面向旧版 1.8/1.12 服务端到当前服务端，但 **MM 本身、SX-Item 与其它依赖仍需选用支持对应服务端的版本**。
低版本不存在的原版属性沿用 SX 的版本门控，不会因施放技能而强行启用。例如旧服可使用攻击力、暴击、吸血，
但不能凭属性技能获得新版本才加入的体型、挖掘速度等原版能力。
Folia 使用实体调度器进入正确区域；增益、治疗和来源操作支持跨区域快照接力，详细边界见下文。

实际核验的 API 版本、指纹与未完成的运行验证见 [兼容性验证记录](../testing/mythic-compatibility.md)。
API 核验不等于这些服务端组合已经实机运行通过。

### Folia 调度与跨区域

同区域技能保持完整同步结算；从全局线程或其他区域线程进入时，先由施法者的 EntityScheduler 接管。
区域归属使用服务端 API 检查，不用距离、相同世界或“主线程”名称推测。

| 场景 | 行为 |
|---|---|
| 同区域 Damage / BaseDamage / PercentDamage / Trigger | 转到实体区域后执行原有完整攻防、事件、脚本与 MM 伤害标记。 |
| 跨区域 Add / Take / Count / Inherit / SourceTime / Update / Heal / SourceRule | 施法者与目标各自采集快照，公式计算后在目标区域执行。 |
| 跨区域三个伤害技能与 Trigger | 在产生副作用前拒绝并输出原因；完整流水线及第三方脚本可能同步读写两端。 |
| 实体迁移、死亡或移除 | 调度跟随实体；各阶段检查存活状态，退役回调结束施法，不继续写入。 |
| 插件停用、调度拒绝、异常、过载 | 终结在途任务并释放引用；最多同时接收 1024 个排队施法。 |

跨区域的 SX `<l:attacker_...>` / `<l:defender_...>` 来自两端各自采样时刻，不是同一 tick 的原子快照；
距离来自采样位置，继承来自施法者采样时的原始来源。采样后的施法者状态变化不会追溯修改已准备的效果。
PAPI 与 SX 参数在公式玩家所属区域求值；Count 读取执行时的目标来源，持续时间从写入时开始，
百分比治疗与最大生命限制取目标执行时的状态。锁定随机结果仍在每个目标的这次施法中共享。

跨区域 MM 标记仅解析 `<caster.字段>` / `<mob.字段>` / `<target.字段>` 中的基础字段：
`name`、`uuid`、`level`、`health`、`hp`、`maxhealth`、`mhp`、`x`、`y`、`z`，
且需当前 MM 版本自身支持该字段。更复杂的目标链、`trigger`、技能变量和第三方占位符会拒绝，
请优先使用 SX 快照变量；这些限制不改变同区域的 MM 标记解析。

接力不会阻塞 Folia tick 线程。MM 收到的成功表示**已接受调度**；效果可能在后续区域 tick 才生效，
后续失败会记录日志，不能把它当作已经命中或回血成功，也不能依赖下一条 MM 技能立即读到效果。
实际伤害开始前才设置 MM 伤害标记，结束后立即恢复，不在排队期间持有标记。

该实现没有跨线程传递 Bukkit 伤害事件、伪造攻击者或直接扣血替代完整攻防结算。
Folia 没有允许任意插件同步操作两个独立区域实体的通用接口；第三方属性、事件监听器与 PAPI 扩展仍需自己兼容 Folia。
参考 [Folia 官方线程模型](https://github.com/PaperMC/Folia#plugin-compatibility)。

## 技能清单

名称不区分大小写。每个技能都是实体目标技能，使用 `@target`、`@self`、`@PlayersInRadius{r=5}` 等 MM 选择器。

| 名称 | 别名 | 用途 |
|---|---|---|
| `SXDamage` | `DamageSX` | 使用本次指定属性造成伤害，默认不附带施法者属性。 |
| `SXBaseDamage` | `BaseDamageSX` | 默认附带施法者的全部当前属性。 |
| `SXPercentDamage` | `PercentDamageSX` | 基础伤害为目标最大生命 × `a`，随后进入 SX 攻防流程。 |
| `SXAttrAdd` | — | 施加命名属性源，支持临时、持久化、叠层和标签。 |
| `SXAttrTake` | — | 移除该技能命名空间内的属性源。 |
| `SXAttrCount` | — | 对已有非持久化来源全部数值乘倍率，再加入额外属性；保留时间、层数和标签。 |
| `SXAttrInherit` | — | 目标继承施法者全部原始属性源快照，可缩放与排除。 |
| `SXAttrSourceTime` | — | 设置已有受管来源的剩余 tick；0 改为不自动到期。 |
| `SXAttrUpdate` | — | 重读目标装备、NBT、槽位等来源并触发属性更新。 |
| `SXAttrTrigger` | — | 触发动态属性定义中的 `API` 事件，复用条件与全部动作类型。 |
| `SXHeal` | — | 固定或按最大生命比例治疗，经过可取消的 Bukkit 回血事件与 SX `HEAL` 触发器。 |
| `SXSourceRule` | — | 执行 `Feature/Source/Config.yml` 中的命名 `Rules` 规则。 |

使用 SX 专属名称避免与其他插件抢占 `DamageAP`、`AttrAdd` 或 MM 原生 `damage`。
本文提供类似 DamageAP 的属性伤害能力，**不把 AttributeMM 的配置原样当作 SX 配置**。

## 快速使用

将下列内容放入 MythicMobs 的 Skills 文件。完整示例可复制 [mythicmobs-skills.yml](../examples/mythicmobs-skills.yml)。
跨早期 MM 4 与 MM 5 建议使用 `|` 分隔属性，并把整条 YAML 技能行加引号。

```yaml
SX_重击:
  Skills:
    - 'SXDamage{al=攻击力=100|暴击几率=50|暴伤增幅=150;pi=true;type=heavy} @target'

SX_普通属性技能:
  Skills:
    - 'SXBaseDamage{bam=1.5;al=攻击力=20} @target'

SX_狂暴:
  Skills:
    - 'SXAttrAdd{s=rage;al=攻击力=30|防御力=10|吸血几率=20;d=200;sm=REFRESH} @self'
```

支持嵌套参数的 MM 版本也可写 `SXDamage{al={攻击力=100;暴击几率=50}}`。
`al` 使用 SX 的 Lore 识别名，包含自定义 `Values.<field>.Match`、插件注册属性以及 JS 属性识别器；
不会把 AttributePlus 的“物理攻击”“真实伤害”等名称自动映射为 SX 属性。
SX 的 `Real` 是“破甲几率”，不是固定数值的真实伤害。

## 参数

### 伤害参数

| 参数 | 别名 | 默认 | 语义 |
|---|---|---|---|
| `attributelist` | `al`, `attributes` | 空 | 仅本次生效的额外属性。支持 `识别名=公式` 或完整 Lore。 |
| `amount` | `a` | 0；百分比技能为 0.1 | 普通技能的基础伤害数值；百分比技能中 `0.1` 表示最大生命的 10%。 |
| `basic` | `b` | BaseDamage 为 true，其它 false | 是否携带施法者当前全部属性，包括装备、默认属性和外部 API 来源。 |
| `basemultiplier` | `bam`, `multiplier`, `m` | 1 | 乘于 `basic` 携带的全部字段，包括触发率；不乘 `al` 或 `a`。 |
| `damagemultiplier` | `dm` | 1 | SX 攻防计算结束后的倍率，随后仍受 Bukkit 护甲、抗性、吸收等修正。 |
| `ignoreimmunity` | `pi` | false | 本次忽略受伤间隔，执行后恢复原有间隔；不绕过事件取消或无敌。 |
| `exclude` | `blacklist` | 空 | 最终攻击快照要移除的内置属性 ID/动态属性 ID，用 `|` 分隔。 |
| `allowself` | — | false | 是否允许对施法者自身造成伤害。 |
| `type` | — | mythic | 自定义伤害标签，提供给脚本/事件；不改变 Bukkit DamageCause。 |
| `args` | — | 空 | 自定义参数列表，用 `|` 分隔，提供给脚本/事件。 |

`al=攻击力=100*1.5` 右侧按公式求值；范围属性请写完整 Lore：`al=攻击力: 10 - 20`。
临时 `al` 单独应用一次动态映射，`basic` 携带的是施法者已映射的结果。
`exclude` 针对最终快照的 ID；排除 `Strength` 不会逆向撤销已经生成的 `Damage`，需要同时排除相应派生 ID。

### 属性来源参数

| 参数 | 别名 | 默认 | 适用范围 |
|---|---|---|---|
| `source` | `s` | 必填 | Add、Take、Count、Inherit、SourceTime。实际来源名统一为 `mythic:<s>`。 |
| `al` | 同上 | 空 | Add 的属性内容；Inherit/Count 可附加额外属性。 |
| `duration` | `d` | 0 | Add、Inherit、SourceTime：整数 tick；20 tick = 1 秒，0 不自动到期。 |
| `stackmode` | `sm` | REPLACE | Add：REPLACE、REFRESH、STACK、MAX、MIN、UNIQUE。 |
| `maxstacks` | `ms` | 1 | Add：正整数最大层数。 |
| `persistent` | `p` | false | Add：沿用来源服务的 SQL/Redis 持久化要求与错误反馈。 |
| `tags` | — | 空 | Add、Inherit：用 `|` 分隔；可被动态属性公式引用。 |
| `multiplier` | `m`, `bam` | 1 | Count、Inherit：全部数值倍率。 |
| `exclude` | `blacklist` | 空 | Count、Inherit：排除的属性 ID。 |
| `rule` | `r` | 必填 | SourceRule：规则 ID，规则中的 Source 由规则自身定义。 |

Inherit 为不同实体间的非持久化 REPLACE 快照，拒绝自身继承以避免循环放大；不能设置 `p=true` 或其它叠层模式，也不能覆盖持久化来源。
它继承装备、NBT、饰品、锻造和外部注册的原始来源；不继承实体默认属性及只在 `SXGetAttributeEvent` 注入的临时结果。
映射在目标汇总时重新计算，从而避免继承力量时把派生攻击计算两遍。
Count 只操作受管的非持久化来源；要改持续时间请另用 SourceTime。
Count 不支持直接计算持久化数组，请用 Add 的文本 REPLACE 更新，以保留可恢复的 Lore 表示。

临时来源沿用来源服务的注销、死亡和重载清理规则。SQL、Redis、版本锁等失败会反馈技能失败并报告原因。
技能不会直接修改装备保留来源；`s=rage` 的删除只移除 `mythic:rage`。
如需用 Take/SourceTime 操作 SourceRule 产生的来源，应在该规则中明确配置 `Source: mythic:规则来源名`。

### 治疗与公式

`SXHeal{a=25}` 恢复 25 点生命；`SXHeal{a=0.2;percent=true}` 恢复目标最大生命的 20%，均不超过当前最大生命。

**所有数值参数共用 SX 自己的参数变量引擎**：`a`、`bam/m`、`dm`、`d`、`ms` 和 `al` 中全部属性值，
适用于上述参数的所有别名与技能。包含旧数组、动态属性及脚本识别的完整 Lore。
使用 `RandomStringManager.Handler`，随机组读取 **SX-Attribute/RandomString**，与 SX 装备词条使用同一套语法。
数值字面量无需公式引擎；SX 表达式、裸数学式和 PAPI 需要安装兼容当前服务端的 SX-Item 并开启 `Settings.FormulaEngine`。

| SX 写法 | 用法示例 |
|---|---|
| 数学计算、嵌套变量 | `a=<c:<l:attacker_Damage_0>*2>`，也可省略外层 `<c:...>`。 |
| 随机整数 | `d=<r:100_200>`、`ms=<i:1_3>`。 |
| 随机小数 | `dm=<d:1_1.5>`。 |
| SX 随机组、内联选择 | `bam=<s:普通基数>`、`ms=<s:1:2:3>`。 |
| 锁定组结果 | `bam=<l:普通基数>;dm=<l:普通基数>`，两个字段使用同一个结果。 |
| 完整 Lore 的随机范围 | `al=攻击力: <r:10_20> - <r:30_40>`。 |
| 按整数 tick 取整 | `d=<c:int <l:普通基数>*200>`。 |

每次施法、每个目标独立建立变量空间；同一目标的数值参数与所有 Lore 行共享 `<l:...>` 锁定结果，
下次施法、另一个目标或嵌套技能重新建立空间。`<s:...>` 等非锁定随机表达式按原生规则分别抽取。
上下文变量名优先于同名随机组。条件、随机多行 Lore 及其它原生表达式由当前安装的 SX 引擎处理；
数值字段的最终结果必须是数字，完整 Lore 保留范围、百分比和多行结构，遵循 `<DeleteLore>` 删除行规则。
`d`、`ms` 在求值后仍必须是整数，不会隐式截断小数；文本/布尔参数以及 MM 选择器自身的参数不属于数值接口。

MM 自身的变量由当前安装版本解析：MM 4 早期使用 `SkillString`，同时将 `<caster.` 转为旧 `<mob.` 前缀；
该版本原本不支持的 MM 变量不会被模拟成有效值。先展开 MM 标记，再将 SX 标记交给 SX 引擎，
可混写 `a=<c:<caster.level>*<l:普通基数>>`。PAPI 使用 SX 的公式玩家：优先施法玩家，否则目标玩家，两端都不是玩家时为空。
以下 SX 变量在全部适配版本中可用：

| 变量 | 示例 |
|---|---|
| 内置数组字段，0 起始 | `<l:attacker_Damage_0>` 为施法者普通最小攻击；`<l:defender_Defense_0>` 为目标普通最小防御。 |
| 动态属性字段 | `<l:attacker_Strength_strength>`、`<l:defender_FireResistance_resistance>`。 |
| 当前/最大生命 | `<l:attacker_health>`、`<l:attacker_max_health>`、`<l:defender_health>`、`<l:defender_max_health>`。 |
| 其它属性上下文变量 | 距离、实体类型、时间、来源层数/剩余时间及标签，见统一属性引擎文档。 |

示例：`SXDamage{al=攻击力=<l:attacker_Damage_0>*2+<l:defender_max_health>*0.05}`。
所有数字必须有限；伤害、倍率、治疗量和持续时间不能为负。非法公式/枚举不会静默改成默认伤害。
不要在原生表达式内部使用未配对的尖括号；条件表达式遵循当前 SX-Item 支持的原生语法。

## 功能覆盖

| SX 功能 | 接入方式与约束 |
|---|---|
| 攻击、PVP/PVE 攻击、命中、暴击、破甲 | Damage 系列复用内置属性优先级与公式，不扣减玩家原版普攻基值，也不因手持弓剔除技能属性。 |
| 闪避、防御、PVP/PVE 防御、格挡、反伤、韧性 | 受击方使用当前实际属性；通过 Add 可给目标施加防御增益。 |
| 吸血、点燃、雷霆、撕裂、攻击药水 | Damage 系列进入原有属性处理器，继续使用原有几率、上限及消息配置。 |
| 元素、抗性、克制、中毒、残血增益与自定义属性 | 所有注册的 `Match` 均可从 al 解析；触发 DAMAGE_ATTACK、DAMAGE_DEFEND、DAMAGE_AFTER。 |
| JS 属性、第三方 SubAttribute | 使用同一旧数组与事件接口；是否执行由该属性声明的类型、配置和脚本决定。 |
| 生命、回复、移速、攻速、幸运、经验、指令属性 | 使用 Add/Inherit 让属性驻留实体来源，由原有 UPDATE/TICK/EXP 等流程执行；写在一次攻击快照里不会变成永久属性。 |
| 高版本原版包装属性 | 通过来源与刷新沿用原有版本检测；低版本没有的原版能力不启用。 |
| Lore、NBT、饰品/RPG 槽位、装备条件 | basic 自动携带已经通过物品条件的当前属性；Update 重读装备来源。技能直接提供的 al 是技能属性，不是一次装备穿戴判定。 |
| 强化、星级、品质、镶嵌、套装、词缀等成长属性 | 当前装备贡献计入 basic 与继承快照；技能不直接替代锻造界面和材料费用流程。 |
| 临时/持久化来源、叠层、标签、来源规则 | Add/Take/Count/Inherit/SourceTime/SourceRule 复用来源服务。 |
| 全部动态动作 | Trigger 触发 API 属性；支持 DAMAGE 以外的治疗、药水、来源、指令、消息、粒子、声音、经验等动作。DAMAGE 动作需要处于伤害事件内。 |

默认不伤害盔甲架、不自伤。显式 SX 伤害技能允许怪物对怪物使用，不受只用于自动普攻的 EVE 开关限制；
伤害原因黑名单仍有效，命中黑名单会取消技能伤害。
伤害由 `LivingEntity.damage(amount, caster)` 发起，保留 Bukkit 取消、减伤、击杀归属和 MM 伤害技能标记。
`dm` 是 SX 计算末尾倍率，内置吸血/反伤等先前步骤使用各自执行时的数值。
同步嵌套的伤害技能最多 8 层，防止伤害触发器无限递归；延迟或下一次独立施法不受影响。

## 脚本与事件

`SXDamageEvent#getData()` 可读取：

```java
// 只有显式 SX 技能携带上下文，普通普攻必须先做判空。
if (event.getData().isSkillDamage()) {
    String type = event.getData().getSkillContext().getType();
    java.util.List<String> args = event.getData().getSkillContext().getArguments();
}
```

JS 属性的 DamageData 同样可读取 `isSkillDamage()` 和 `getSkillContext()`。
伤害快照在本次原生事件结束后释放，不向实体写入持久化 metadata。
上下文中的取消与 DAMAGE_AFTER 均指当前 SX 阶段，后续其它 Bukkit 监听器仍可能调整或取消事件。
