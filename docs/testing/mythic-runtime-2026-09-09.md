# MythicMobs 真服验证（2026-09-09）

基线：已拉取并确认 `origin/3.x` 的 `58404686`。本次在此基础上修复两个运行问题，未提交或推送。
用户明确授权构建、运行；测试目录为 `E:\Minecraft-Server\incisionTest\sxattribute-mythic-results`。
所有节点均为原测试服的独立副本，监听 `127.0.0.1`，测试后正常停服；原节点插件和世界没有替换。

## 修复

1. MM 4 的 Bukkit 监听器在 HIGHEST 用 `ActiveMob.lastDamageSkillAmount` 覆盖伤害。
   旧实现只设置 `isUsingDamageSkill`，导致 SX 在 HIGH 算出的 20/60/12 被覆盖为 0。
   现在在 SX 结算完成后同步 MM 数值槽，并在调用结束、取消或异常时恢复原标记、数值和外层作用域。
2. MM 5.13 的 `isSet()` 不区分大小写，配置仅含 `SX-Attribute` 和 `sxattribute` 时，
   原实现把小写节点当作 `SXAttribute`，取到 888 而不是预期 12。
   现在使用配置实际键集合判断优先级，仍由 MM 读取合并后的配置值。

## 服务端矩阵

以下均非 Folia。每台分别执行无 MM 启动、带 MM 冷启动、实体断言、`mm reload` 和重复断言。

| 服务端 | Java | MM | 冷启动断言 | 重载后断言 |
| --- | --- | --- | --- | --- |
| Paper 1.12.2 build 1620 | Zulu 8u472 | 4.11.0-2fb2bf23 | 29/29 | 29/29 |
| Paper 1.16.5 build 794 | 11.0.0.2 | 4.11.0-2fb2bf23 | 29/29 | 29/29 |
| Paper 1.20.6 build 151 | Zulu 21.0.10 | 5.8.0 | 28/28 | 28/28 |
| Paper 1.21.11 build 132 | Zulu 21.0.10 | 5.13.0-aad3f1c9 | 28/28 | 28/28 |
| Spigot 26.1.2 | 25.0.1 | 5.12.1 | 28/28 | 28/28 |
| Leaf 26.2 build 42 | 25.0.1 | 5.13.0-aad3f1c9 | 28/28 | 28/28 |
| Paper 26.2 build 84 | 25.0.1 | 5.13.0-aad3f1c9 | 28/28 | 28/28 |

合计 396 个真服断言通过。MM 4 多一个原生伤害数值槽恢复断言。
服务端目录中没有 1.21.0；用户提供的 5.10.0 完成 ABI 检查，未伪报为 1.21.0 真服通过。
Modrinth 的 5.10.0 支持范围为 1.21 至 1.21.8，实际 1.21.11 节点使用 5.13.0。

## 配置与结果

完整配置、探针源码和执行脚本在测试目录的 `probe/`、`run_matrix.py` 中；每个节点保留
`test-console.log`、`result.json` 和完整测试配置。汇总文件 `final-matrix.json` 记录启动命令、
Java 输出、每次断言的实际值和所有已安装插件的 SHA-256。

| 行为 / 关键配置 | 预期 | 实际 |
| --- | --- | --- |
| `SXAttribute: ['攻击力=<l:mob_level>*10']`，等级 3；另设两个冲突别名 | 30 | 30 |
| `SXAttribute: []`，另设非空别名 | 无出生来源 | 无出生来源 |
| `SX-Attribute` 为 12，小写节点为 888 | 12 | 12 |
| 仅小写节点 `攻击力=14` | 14 | 14 |
| 首行有效、次行未定义变量 | 不写半份来源 | 来源不存在，并记录配置告警 |
| 重复通知把攻击改为 999 | 保持原来源对象与 30 | 保持 |
| `攻击力: 10 - 20`、`暴击几率: 50%` | 10/20、50 | 相符 |
| 同怪攻击/防御都用 `<l:SXProbeLock>` | 两行相同，两个怪物独立 | 相符，日志保存抽取值 |
| `SX-Equipment: ['SXProbeSword:HAND']`，武器攻击 7；出生公式为装备攻击 ×2 | 出生来源 14 | 14 |
| `SXAttrAdd` 使用 `<c:5*2>`，STACK 3 层上限；时间/层数也用公式 | 首次 10，再次 20 | 10、20 |
| `SXAttrCount` 倍率 1.5，再加 5 | 35 | 35 |
| `SXAttrSourceTime`、`SXAttrUpdate` | 保留 35 及独立出生来源 | 保留 |
| `SXAttrInherit` 比例 0.5 | 来源至少 32.5 | 32.5 |
| `SXAttrTake` | 移除指定 Buff | 已移除 |
| `SXAttrAdd` 动态属性 `力量=5` | Strength.strength 为 5 | 5 |
| `SXHeal{a=<c:5*2>}` | 50 → 60 | 60 |
| 无敌目标的原生及 SX 伤害 | 不扣血 | 不扣血 |
| `SXDamage` / `SXBaseDamage` / `SXPercentDamage` | 易伤测试目标实际扣血 | 全部扣血，日志记录原生事件及最终血量 |
| `SXSourceRule{r=BattleRage}` | 产生 buff:battle_rage 来源 | 攻击 5 |
| 死亡 | 出生来源清理 | 已清理 |
| 施法结束 | MM 技能标记恢复；MM 4 数值槽恢复 | 相符 |

`SXAttrTrigger` 已通过实际 MM 加载及调用，但本探针未配置可观察的 API 动作，不能把无异常等同于动作效果验收。

## 构建与静态检查

- 使用 JDK 25.0.1、已安装的 Gradle 9.6.1、`D:\GradleCache` 离线执行 `build`，48 项 JUnit 全部通过。
- 本机 Windows Unix socket 存在 `Invalid argument: connect`，使用进程级
  `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=Z:\nonexistent-sx-test` 让 JDK 回退 TCP；没有修改全局 Java 配置。
- 5 份实际 MM 制品通过更新后的 122 项 ABI 检查，结果在测试目录 `runtime-abi.json`。
  新增检查包括 MM 4 的数值槽访问器和 MM 5 的 `getKeys(String)`。
- `git diff --check` 通过。
- 最终制品：`build/libs/SX-Attribute-4.0.0-beta.9-all.jar`。
- SHA-256：`593ef3da0b0fde1ab39a4562d00c75c942742e05c8a138ce57e2d86dae491c76`。

## 环境限制

- MM 5.13.0 不支持 1.20.6 的 NMS，必须使用本矩阵的 5.8.0 等匹配版本。
- MM 5.13.0 在纯 Spigot 26.1.2 调用 Paper 专有 `MythicLoadedEvent.callEvent()` 失败；
  使用从 Modrinth 下载并核验 SHA-512 的 5.12.1 后通过。
- MM 4.11.0 在 1.12.2 会报告自身 `EntityTransformEvent` 不存在及默认 GOLDEN_HELMET 示例不兼容；
  SX 本次测试通过不代表 MM 的所有内置功能都兼容。
- 1.16.5 使用 Java 11。此前 Java 16 运行留下的空 MM `active-mobs.json` 已在独立测试副本中备份重建。
- 无玩家的 Spigot 测试必须先为区块加加载票据并等待进入实体 tick 状态；只调用 `Chunk.load()` 不足。
- MM 新生成实体有短暂无敌状态。探针先验证无敌保护，再对测试实体关闭无敌进行伤害断言。
- Paper 的旧版本查询地址、Google Maven 镜像和 Leaf 的 Windows 性能计数器会产生环境告警，
  不计为 SX 功能失败；依赖下载使用进程级 Maven Central 地址。

## 未覆盖

## 追加：真实玩家触发的多段伤害与快照验收

使用 `E:\Minecraft-Server\incisionTest\devdump` 中的 Mineflayer 客户端实际登录，
通过玩家 `/mm mobs spawn` 生成怪物，再发送真实攻击包，由怪物 `~onDamaged` 触发 MM 技能 YAML。
本轮运行期间禁用 `SXMythicProbe`，没有通过辅助插件调用 MM/SX API 或直接制造伤害。
原七节点分别执行冷启动和 `/mm reload` 后四个场景，合计 **56/56 通过**。
26.x 使用已有 ViaVersion/ViaBackwards 连接 1.21.11 客户端；其余节点使用对应原生协议。

| 场景 | MM 配置行为 | 预期玩家血量 | 实际 |
| --- | --- | --- | --- |
| 同 tick 三连击 | 连续三次 `SXDamage{a=2;pi=true}` | 20 → 14 | 七节点两轮均相符 |
| 延迟段间属性变化 | 基础攻击 2，延迟 12 tick 加攻击 2，再延迟移除 | 20 → 18 → 14 → 12 | 七节点两轮均相符 |
| MM 原生重复调度 | `SXDamage{a=2;pi=true;repeat=2;repeatInterval=12}` | 20 → 18 → 16 → 14 | 七节点两轮均相符 |
| 单段快照隔离 | 首段基础攻击 ×2 再加 3，后两段恢复基础攻击 2 | 20 → 13 → 11 → 9 | 七节点两轮均相符 |

快照语义为每次伤害调用独立取值，并非整个延迟技能链固定使用首次属性。
倍率和额外属性没有污染后续段使用的施法者来源。延迟场景逐一核对客户端血量包；
同 tick 场景部分新协议合并中间血量包，因此只断言累计扣血，不声称客户端逐包观察到三个事件。

测试脚本和 YAML：`devdump/mythic-player-e2e.js`、`mythic-player-matrix.py`、
`mythic-player-mobs.yml`、`mythic-player-skills.yml`。
结果：`sxattribute-mythic-results/player-matrix.json`，各节点保留
`player-result.json`、`player-client.log`、`player-console.log`。
运行使用独立副本，关闭自然刷怪/回血并清理副本内非玩家实体；测试进程均已停止。
环境校准阶段曾有遗留僵尸、苦力怕和新版本游戏规则改名导致的干扰，最终结果来自修正环境后的运行。

Spigot 26.1.2 / MM 5.12.1 的玩家生成命令在怪物已生成后，成功消息路径
`io.lumine.mythic.bukkit.utils.text.Text.sendMessage` 抛出 `ArrayStoreException: CraftPlayer`。
该 MM 自身问题仍存在，不能将技能数值通过解释成整个 MM 玩家命令完全兼容。

本追加覆盖上述四类玩家场景，不覆盖同步嵌套施法、跨目标并发、投射物快照、Folia 区域切换，
也不替代下面列出的完整战斗、持久化和生命周期待测项。

## 仍未覆盖

### Beta 10 发布前的补充结果

本次补测发现并修复吸血、格挡、反射的比例修正误用概率值的问题，以及 MM 参数将减号、空格编码为
`<&da>`、`<&csp>` 后导致 SX 减法计算错误的问题。修复后的固定减法、Lore 减法和共享锁定随机在
Paper 1.20.6 / MM 5.8.0 的真实玩家窄矩阵中通过。临时诊断日志已移除。

反伤仍有未解决偏差：普通难度下公式输出 2 点，玩家实际扣血 3 点，原因尚未确认。
此前将多出的 1 点解释为首击累计的判断已撤回。86 个案例的完整七节点冷启动/reload 矩阵尚未跑完；
新增的外部 PAPI 玩家变量案例仅已生成，不能视为已通过。Beta 10 发布不代表所有战斗效果或变量组合完成验收。

上述历史构建 SHA-256 属于此前的 Beta 9 测试制品；Beta 10 的正式下载制品以 GitHub Release 为准。

没有 Folia 节点，因此跨区域接力、传送/下线/退役和停用竞态仅有现有单元测试覆盖，未做 Folia 真服验证。
未完成真实玩家战斗矩阵、逐项暴击/破甲/闪避/吸血/反伤/元素效果、PAPI、所有变量组合、
持久化跨重启/多服、来源到期计时、API Trigger 动作效果和 MM 4.1/4.9/4.14/5.0/5.2 的真服矩阵。
这些项目不能由 396 项已通过断言代替，也不能由旧版 ABI 记录推断为已实测。
