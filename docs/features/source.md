# 属性来源与多服存储

来源（Source）是独立于装备 Lore 的属性载体，适用于技能、职业、临时 Buff、跨服状态和外部插件注入。来源键相同的写入遵循所选叠层策略，名称不同的来源互不覆盖。

## 配置入口

`Feature/Source/Config.yml` 控制总开关、检查周期、服务器 ID、持久化后端、Redis 与来源模板。

```yml
Rules:
  BattleRage:
    Source: buff:battle_rage
    Duration: "<c:200>"
    MaxStacks: "<c:5>"
    StackMode: STACK
    Persistent: false
    Tags: [ rage ]
    Attributes:
      - "攻击力: 5"
```

`Duration` 的单位为 tick；`Persistent: false` 仅保存在当前服务器内存，`true` 才写入配置的 Repository。

## 生命周期与叠层

支持以下 `StackMode`：

| 模式 | 行为 |
|---|---|
| `REPLACE` | 用本次来源完全替换旧状态。 |
| `REFRESH` | 保留层数，刷新持续时间。 |
| `STACK` | 增加层数，受 `MaxStacks` 限制。 |
| `MAX` / `MIN` | 比较新旧来源的战力值；旧值更优时保留旧属性与层数，仅刷新持续时间。 |
| `UNIQUE` | 已存在时不重复施加。 |

来源标签会进入公式上下文，例如 `defender_tag_rage`。每个来源还提供 `attacker_source_buff_battle_rage_stacks`、`attacker_source_buff_battle_rage_remaining` 一类变量；剩余时间以 tick 表示，永不过期为 `-1`。到期来源由 `CheckPeriodTicks` 定时清理；来源变化会触发对应 API 事件。

## 存储模式

| `Storage.Type` | 场景 | 约束 |
|---|---|---|
| `MEMORY` | 临时测试或纯内存来源 | 重启后丢失。 |
| `SQLITE` | 单服持久化 | 不允许开启 `NetworkMode`。 |
| `MYSQL` | 多服或独立数据库 | 使用事务和乐观版本控制。 |
| `POSTGRESQL` | 多服或独立数据库 | 使用事务和乐观版本控制。 |

SQL 记录玩家、来源键、属性负载、层数、到期时间、版本与写入服务器。开启 `NetworkMode: true` 时还必须启用 Redis；Redis 负责分布式锁和失效广播，断开后允许读取但拒绝持久化写入。`Persistent: true` 配合 `Storage.Type: MEMORY`，或在网络模式中使用 SQLite，都会返回 `STORAGE_DISABLED`。

## API 与迁移

旧 `SXAPI.addSourceAttribute`、`takeSourceAttribute` 仍可用。新 API 可按规则应用受管来源，外部插件无需自行处理叠层或 SQL：

```java
SXAttribute.getApi().applyManagedSourceRule(player, "BattleRage");
```

首次升级时请备份旧 PDC/YAML 来源数据。成功导入后会保留迁移标记和备份，防止重复导入。
