# Feature

SX-Attribute 的配置化功能层。属性、元素、克制、特效、Buff、Debuff 与条件统一由属性 DSL 表达；装备成长模块只维护 SX-Item NBT 状态并生成属性 Lore。

该模块用于消除“一种玩法一个监听器/属性类”的扩展成本，使服主可以只修改配置完成数值与玩法组合。

## 核心能力

- `attribute`：动态属性字段、原子热重载、条件公式和白名单动作。
- `source`：命名源叠层、到期、SQLite/MySQL/PostgreSQL 持久化与 Redis 多服协调。
- `equipment`：套装、品质、词缀、强化、升星、重铸、洗练、宝石孔和附魔成长。

## 配置入口

- `Feature/Attribute/Attributes.yml`：属性分片聚合清单。
- `Feature/Source/Config.yml`：来源生命周期与存储后端。
- `Feature/<装备功能>/Config.yml`：独立开关、公式、成本和玩法规则。

## API 示例

```java
double fireDamage = SXAttribute.getApi().getDynamicAttribute(player, "FireDamage", "damage");
SourceWriteResult result = SXAttribute.getApi().applyManagedSource(player,
        new SourceApplyRequest("skill:rage", Arrays.asList("攻击力: 10"),
                200L, 3, SourceApplyRequest.StackMode.STACK, false,
                Collections.singletonList("rage")));
```

## 运行约束

- 属性普通定义支持 `/sxa reload` 完整热重载。
- 自定义附魔 ID 必须在 Minecraft 注册表冻结前注册，新增或删除 ID 需要完整重启；首个 Beta 未提供跨版本注册适配器，未预注册的 ID 会被禁用。
- 多服模式下 Redis 不可用时持久化写入会被拒绝，读取仍使用 SQL 事实源。
- Folia 实体更新必须通过实体调度器执行。

## 依赖关系

- 必需：SX-Item 的表达式、NBT Wrapper 与物品更新协议。
- 可选：Vault、Redis、SQLite/MySQL/PostgreSQL 驱动。
- 被核心属性管理器、伤害监听器、Placeholder 和 `/sxa forge` 调用。

详见 [DESIGN.md](DESIGN.md)。
