# 配置化功能文档

SX-Attribute 4 的新增功能以 `Feature/` 目录为配置入口。所有装备状态写入物品 NBT，Lore 只由状态动态重建；普通属性定义支持 `/sxa reload` 热重载。

## 索引

| 文档 | 适用内容 | 主要配置入口 |
|---|---|---|
| [统一属性引擎](attribute.md) | 自定义属性、元素、克制、特效、Buff、Debuff、条件与动作 | `Feature/Attribute/` |
| [属性来源与存储](source.md) | 来源生命周期、叠层、SQLite/MySQL/PostgreSQL、Redis 多服 | `Feature/Source/Config.yml` |
| [锻造与装备成长](equipment.md) | 套装、品质、词缀、强化、升星、重铸、洗练、宝石孔、附魔成长 | `Feature/<模块>/` |
| [主配置与安装](../configuration.md) | 依赖、`Config.yml`、消息与重载边界 | 根 `Config.yml` / `Message.yml` |
| [完整属性配置](../attributes.md) | 内置属性、Lore 文本、原版属性与动态定义 | `Feature/Attribute/definitions/` |
| [指令、权限与 Placeholder](../commands.md) | 命令入口、权限节点与变量 | `plugin.yml` / `Message.yml` |

## 快速开始

1. 复制发布包中的 `Feature/` 到插件数据目录；已有服请先备份 `plugins/SX-Attribute/`。
2. 在各模块 `Config.yml` 中设置 `Enable: true/false`，模块关闭后 NBT 会保留，但不再提供属性或 GUI 操作。
3. 使用 `/sxa forge` 打开锻造 GUI；使用 `/sxa reload` 重载普通配置和规则。新增或删除真实自定义附魔 ID 仍建议完整重启。
4. 完成配置后用测试物品验证 Lore、成本与战斗结果，再迁移到正式服。

## 共通约束

- 公式使用 SX-Item 表达式语法；条件支持 `>`、`<`、`>=`、`<=`、`==`、`!=`、`&&` 与 `||`。
- 配置中的属性文本必须能被属性定义识别，例如 `攻击力: 10`。
- 锻造 GUI 会锁定玩家会话并校验主手物品指纹；成本扣除或写入异常会回滚。
- 多服模式必须同时配置 SQL 与 Redis；Redis 不可用时拒绝持久化写入，避免跨服并发覆盖。
