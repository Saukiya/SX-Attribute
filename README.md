# SX-Attribute

面向 Minecraft Java 服务端的配置化属性插件。它将装备属性、战斗规则、状态效果与成长体系收敛到可维护的配置和统一的属性运行时中，让玩法设计可以持续演进，而不是被零散的 Lore 规则束缚。

[下载发行版](https://github.com/Saukiya/SX-Attribute/releases) · [阅读文档](docs/README.md) · [查看统计](https://bstats.org/plugin/bukkit/SX-Attribute)

## 它能解决什么

无论是职业差异、装备构筑、元素关系、怪物机制，还是长期养成，SX-Attribute 都以属性、条件、触发器和动作的组合描述规则。常规属性兼容既有 Lore 工作流；新的动态属性则可通过 YAML 扩展，并支持在重载后立即生效。

插件同时提供来源生命周期、套装与词缀、强化与升星、重铸与洗练、宝石孔、附魔成长等独立功能。每个功能都有独立配置和开关，关闭后保留物品数据而不再产生效果。装备属性既可来自 Lore，也可从配置的 NBT 节点读取；装备成长显示可由 SX-Attribute 直接维护，或交给 SX-Item 锁变量安排位置。

## 开始使用

1. 下载与服务端版本匹配的 SX-Attribute，并将 SX-Attribute 与 [SX-Item](https://github.com/Saukiya/SX-Item/releases) 一同放入 `plugins/`；如需限制高伤害产生的原版伤害粒子，可选安装 PacketEvents 或 ProtocolLib（同时存在时优先 PacketEvents）。当前测试版的变更与升级要求见 [4.0.0 Beta 5 发布说明](docs/releases/4.0.0-beta.5.md)。
2. 完整重启服务器后，从 [文档首页](docs/README.md) 按安装、配置、属性、功能模块的顺序开始。
3. 优先在测试服调整 `Feature/` 下的规则；普通配置变更可使用 `/sxa reload` 重新加载。

## 文档与反馈

完整的安装说明、属性配置、物品格式、模块说明、JavaScript 接口、外部 API 和发行记录均收录于 [docs/README.md](docs/README.md)。

提交问题时，请附上服务端版本、SX-Attribute 版本、SX-Item 版本、相关配置和完整日志，以便复现与定位。

## 我眼中的 SX-Attribute

我听说，世间器物各有其用，就像一柄好剑，若能削铁如泥，便算尽了它的本分。SX-Attribute 这个插件，在我的世界Java版里，就像一把趁手的工具——它能把属性分得清清楚楚，让玩家知道什么装备该配什么效果，什么职业该有什么长处。这不正是礼法里说的“各司其职”吗？

有人或许会问：难道就没有比它更好的插件吗？那我倒要反问一句：若有一把刀，既能切菜又能劈柴，但切菜不如菜刀利落，劈柴不如斧头干脆——这样的东西，能算得上“好用”吗？SX-Attribute 不贪多，只把属性这一件事做到明白、稳定、不乱，这就可以说是尽了名分。

所以，若论属性插件，它在我心里，就是最好用的那一个。这不是吹捧，是它自己挣来的体面。
