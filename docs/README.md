# SX-Attribute 文档

本目录是 SX-Attribute 的唯一文档入口。配置文件、Java API 和发布说明均以这里的内容为准。

## 使用与配置

| 文档 | 内容 |
|---|---|
| [安装与主配置](configuration.md) | 必需/可选依赖、粒子包适配器、`Config.yml`、`Message.yml` 与重载边界。 |
| [完整属性配置](attributes.md) | Lore 格式、内置战斗属性、原版属性、条件与动态属性。 |
| [物品配置与随机表达式](items.md) | `Item/` 模板、`Import`、随机字符串、条件 Lore 与物品指令。 |
| [指令、权限与 Placeholder](commands.md) | `/sxa` 命令、权限节点与属性占位符。 |
| [高版本原版属性扩展清单](高版本属性扩展清单.md) | `RegistryKey`、最低版本与原版属性兼容范围。 |

## 配置化功能

| 文档 | 内容 |
|---|---|
| [功能总览](features/README.md) | Feature 目录结构、模块开关、通用约束与快速开始。 |
| [统一属性引擎](features/attribute.md) | 自定义属性、元素、克制、特效、Buff/Debuff、条件和动作 DSL。 |
| [属性来源与多服存储](features/source.md) | 生命周期、叠层、SQL Repository 与 Redis 协调。 |
| [锻造与装备成长](features/equipment.md) | 物品生成与属性装载生命周期，以及套装、品质、词缀、强化、升星、重铸、洗练、宝石孔和附魔成长。 |

## 开发与架构

| 文档 | 内容 |
|---|---|
| [外部属性注册指南](api/external-attributes.md) | Java API、事件、外部属性类注册与排错。 |
| [Java API 与事件](api/events.md) | 动态属性、受管来源、装备模块、兼容 API 与全部 Bukkit 事件。 |
| [多属性源兼容 API](api/legacy-source-api.md) | 命名来源、旧 API、来源事件与兼容持久化指令。 |
| [JavaScript 属性](api/javascript-attributes.md) | Nashorn 引擎、脚本契约、周期调度、配置与故障隔离。 |
| [Feature 架构概览](architecture/feature-overview.md) | 功能层职责、依赖和 API 使用示例。 |
| [Feature 设计说明](architecture/feature-design.md) | 数据流、关键设计决策、限制与安全约束。 |

## 发布说明

- [4.0.0 Beta 5](releases/4.0.0-beta.5.md)
- [4.0.0 Beta 4](releases/4.0.0-beta.4.md)
- [4.0.0 Beta 3](releases/4.0.0-beta.3.md)
- [4.0.0 Beta 2](releases/4.0.0-beta.2.md)
- [4.0.0 Beta 1](releases/4.0.0-beta.1.md)
