# SX-Attribute 3+

[Minecraft-Plugin] 属性管理插件

[下载](https://github.com/Saukiya/SX-Attribute/releases/tag/3.7.0) ·
[文档](https://github.com/Saukiya/SX-Attribute/tree/2.x?tab=readme-ov-file#tutorial) ·
[统计](https://bstats.org/plugin/bukkit/SX-Attribute)

文档部分已过时.

## 配置化 Feature 引擎

- 属性主清单迁移为 `Feature/Attribute/Attributes.yml`，按顺序聚合 `definitions/*.yml`。
- 自定义属性可声明命名字段、事件触发器、条件公式和白名单动作，用同一套定义表达元素、克制、特效、Buff 与 Debuff。
- `Feature/Source/Config.yml` 可选择内存、SQLite、MySQL 或 PostgreSQL；多服模式使用 Redis 写锁与失效广播。
- `/sxa forge` 打开装备成长 GUI；套装、品质、词缀、强化、升星、重铸、洗练、宝石孔和附魔成长均有独立目录与开关。
- 装备成长状态写入 SX-Item NBT，Lore 只作为动态渲染结果。

模块结构、API 和多服一致性约束见 `src/main/java/github/saukiya/sxattribute/feature/README.md`。

我听说，世间器物各有其用，就像一柄好剑，若能削铁如泥，便算尽了它的本分。SX-Attribute 这个插件，在我的世界Java版里，就像一把趁手的工具——它能把属性分得清清楚楚，让玩家知道什么装备该配什么效果，什么职业该有什么长处。这不正是礼法里说的“各司其职”吗？

有人或许会问：难道就没有比它更好的插件吗？那我倒要反问一句：若有一把刀，既能切菜又能劈柴，但切菜不如菜刀利落，劈柴不如斧头干脆——这样的东西，能算得上“好用”吗？SX-Attribute 不贪多，只把属性这一件事做到明白、稳定、不乱，这就可以说是尽了名分。

所以，若论属性插件，它在我心里，就是最好用的那一个。这不是吹捧，是它自己挣来的体面。

## Support Server

- Spigot
- Paper
- Folia
- More...

## Support version
- [This](https://github.com/Saukiya/SX-Item/releases)

有问题请提交到issue.

## 多属性源 API (Multi AttributeSource)

一个实体的最终属性 = **若干独立命名源之和**。每个源有唯一标识名(source key), 可被单独添加/更新/移除而互不干扰; 同名源重复写入 = **替换而非叠加**。内部装备/手持/槽位、外部脚本/职业/药水、旧插件 API、抛射物都统一为命名源。

| 来源标识 | 场景 |
|---|---|
| `装备-主手` / `装备-副手` / `装备-盔甲` / `槽位` / `RPG栏` | 内部物品(按位置分组, 每次加载自动重建) |
| `力量加成` 等自定义名 | 脚本/插件通过 API 注入的动态属性 |
| `class:<全类名>` | 旧 `setEntityAPIData(Class,…)` 映射的兼容源 |
| `抛射物` | 弓箭等抛射物快照 |

> 内部物品源在每次装备变动时只清除并重建自己, **不影响**脚本/职业等外部源(独立生命周期)。

### 获取 API

```java
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.api.SXAPI;

SXAPI api = SXAttribute.getApi();
```

### 方法一览

| 方法 | 说明 |
|---|---|
| `boolean addSourceAttribute(LivingEntity, String source, List<String> lore, boolean update)` | 解析 lore 词条为源并添加/覆盖; 触发**可取消**的 `SXAttributeSourceAddEvent` |
| `boolean addSourceAttribute(LivingEntity, String source, SXAttributeData data, boolean update)` | 直接用数据添加/覆盖 |
| `void createStaticAttributeSource(LivingEntity, String source, List<String>/SXAttributeData, boolean update)` | 创建**静态源**(不触发事件的纯数值注入) |
| `SXAttributeData takeSourceAttribute(LivingEntity, String source[, boolean update])` | 移除源(非静态触发 `SXAttributeSourceRemoveEvent`), 返回被移除数据 |
| `SXAttributeData getSourceAttribute(UUID, String source)` | 读取某源数据(无则 null) |
| `boolean hasSourceAttribute(UUID, String source)` | 是否存在某源 |
| `Set<String> getSourceNames(UUID)` | 实体全部源名 |
| `SXAttributeData loadListData(List<String>)` | 把词条行解析成 `SXAttributeData`(供上面直接数据版复用) |

> `update` = 是否立即刷新 UPDATE 类属性(体型/攻速/生命上限等)。战斗几率类属性在受击/攻击时实时读取, 无需 update。

### 示例

```java
SXAPI api = SXAttribute.getApi();

// 1. 脚本"力量转攻击": 每次直接覆盖同名源, 永不叠加
api.addSourceAttribute(player, "力量加成", java.util.Arrays.asList("攻击伤害: 50"), true);

// 2. 单独移除该来源, 其它源不受影响
api.takeSourceAttribute(player, "力量加成");

// 3. 静态源: 内部计算后批量注入, 不触发事件
SXAttributeData data = api.loadListData(java.util.Arrays.asList("生命上限: 100", "攻击距离: 2"));
api.createStaticAttributeSource(player, "职业系统", data, true);

// 4. 查询
boolean has = api.hasSourceAttribute(player.getUniqueId(), "力量加成");
java.util.Set<String> names = api.getSourceNames(player.getUniqueId());
```

### 事件

```java
// 添加源(可取消 / 可改注入值), 静态源不触发
@EventHandler
public void onAdd(SXAttributeSourceAddEvent e) {
    e.getEntity(); e.getSource();
    e.setData(e.getData());   // 可调整注入数据
    e.setCancelled(true);     // 可拦截注入
}

// 移除源(仅通知)
@EventHandler
public void onRemove(SXAttributeSourceRemoveEvent e) {
    e.getEntity(); e.getSource(); e.getData();
}
```

### 无叠加保证

命名源独立键存 → 同名 `add` 即替换; `take` 精确移除; 每次取属性全量重算汇总; UPDATE 类属性按默认值绝对重算。脚本每 tick 覆盖同名源即可, 无需手动先删。

> 兼容: 旧的 Class 分源 API(`setEntityAPIData` 等)与抛射物 API 均已重写为委托新命名源, 行为等价、无需改动既有调用。唯一破坏性点: 若曾用 `getEntityDataMap().put(...)` 直接塞数据, 请改用 `addSourceAttribute`。

### 指令 (主命令别名 `/sxa` `/sx` `/sa`)

| 指令 | 说明 |
|---|---|
| `/sxa source [玩家]` | 聊天栏查看玩家各属性来源及其贡献的非零属性 |
| `/sxa statssource [玩家]` | 打开"属性源统计"面板(每个源一格, 展示其属性) |
| `/sxa update [玩家]` | 重新载入装备源并刷新其 UPDATE 类属性 |
| `/sxa persistent <玩家> <源名> <属性词条...>` | 新增**持久化**属性源(存盘, 跨重连/重启存活); 多条属性用英文逗号分隔 |
| `/sxa del-persistent <玩家> <源名>` | 删除一个持久化属性源 |

```
# 例: 给 Steve 永久 +50 攻击、+10% 暴击几率
/sxa persistent Steve 力量加成 攻击伤害: 50, 暴击几率: 10
/sxa source Steve            # 查看来源
/sxa del-persistent Steve 力量加成
```

> 持久化源存储: 高版本(1.14+)用 MC 原生 `PersistentDataContainer`(随玩家存档自动持久化); 低版本回退到每玩家独立 yml(`PersistentSource/<uuid>.yml`)。玩家上线自动重新施加; 与装备/脚本源互不干扰。省略 `[玩家]` 时默认对自己操作。
