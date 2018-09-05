<h1 align="center">
  <br>
  <br>
  SX-Attribute - 物品属性插件
  <h5 align="center">
<a href="#introduction">插件简介</a>&nbsp;&nbsp;
<a href="#features">插件特点</a>&nbsp;&nbsp;
<a href="#config">指令配置</a>&nbsp;&nbsp;
<a href="#tags">标签介绍</a>&nbsp;&nbsp;
<a href="#tutorial">详细教程</a>&nbsp;&nbsp;
<a href="#downloads">下载链接</a>&nbsp;&nbsp;
<a href="#development">开发文档</a>&nbsp;&nbsp;
<a href="#license">开源条款</a>
</h5>
  <h5 align="center">修改日期: 2018/09/04</h5>
  <br>
  <br>
  <br>
  <br>
  <br>
</h1>

<a name="introduction"></a>
## 插件简介

* SX-Attribute是一款强大的 RPG属性插件，它可以为你的服务器提供 34+属性标签，通过随机算法而诞生的品质系统、物品关联职业系统是本插件最大特色。
* 其次，可以设置每个武器的攻击速度，配合1.9新特性，战斗中可显示触发效果面板，能让玩家感受到更多的战斗乐趣。并且可以通过权限(职业)、主副手来限制使用它，异步计算属性数据以减少主线程负担，长期的改善减少了大量的bug，并且支持 RPGInventory 装备识别、 Mythicmobs 穿戴及掉落、SkillAPI血量兼容。希望你会喜欢。
* 重制后的SX-Attribute 可以外部注册新属性以及新规则标签，并且处理每个标签的优先级，并且能设计出大量的附属。
(宝石/耐久/排行/魔法/天赋/等等)
* [MCBBS](http://www.mcbbs.net/thread-793362-1-1.html)

<br>

<a name="features"></a>
## 插件特点

* 多达 34+种属性标签，可直接在任何物品lore内生效
* 可以将 全部属性 计算为战斗点数，所有属性支持PlaceholderAPI变量
* 支持主手/副手、职业判定、等级限制使用！
* 属性同时对怪物生效，可以将设置在Myticmobs怪物的装备中
* 异步计算属性数据，减少服务器耗能
* 属性支持正负加减
* 可以删除原版默认护甲、攻击力，不是隐藏而是删除！并且支持全服清理原版默认标签！
* 支持原版1.9伤害计量器特性!根据蓄力时长获得满额伤害!可通过Lore调整攻击速度!
* 血条压缩功能，可设定压缩血条量，血量低于压缩值自动不压缩
* 全息显示伤害信息面板，最多有15种效果显示，并且会在右侧显示，不会阻碍视线
* 触发效果会提示攻击方、受伤方，可以是聊天框、Title、Actionbar三种显示方案
* GUI展示全部属性，并且可以100%自定义GUI内的文本！支持PlaceholderAPI变量
* 掉落物名字展示，可以只展示有名字的掉落物！
* 可以在Config中禁止盾牌右键造成的无敌bug，对于rpg服有很好的效果
* 随机数值，随机字符串组，可以用在任意区域(Name/Lore)，让每个武器都不相同
* 支持Mythicmobs掉落，在怪物掉落内填写 "* sx 物品编号 数量(可随机) 几率"即可
* 支持RPGInventory装备读取，读取GUI内所有物品，可设置部分格子不读取。
* 更方便的搜索存储物品，支持保存附魔/头颅/ItemFlag。更好的管理数据，带搜索功能
* 类似RPGItems的物品更新机制，此功能对于有镶嵌强化系统的服务器需慎用，默认关闭
* 为开发者提供一套完整的注册属性API、属性更新事件
* 100%自定义消息文件，可自由切换玩家消息的输出方式(Message/Title/Actionbar)
* 以上功能均可在设置内开启关闭

<br>

<a name="config"></a>
## 基本信息 Essential Information

#### 指令 (Command): 

* /sx stats    : 查看属性
* /sx repair   : 打开修理界面
* /sx sell     : 打开售出界面
* /sx give <ItemName> <Player> <Amount>  : 给予玩家RPG物品
* /sx save <ItemName> : 保存当前的物品到配置文件
* /sx nbt   : 重新加载这个插件的配置
* /sx displaySlot   : 显示可装在物品的槽位
* /sx attributeList   : 查看当前属性列表
* /sx conditionList   : 查看当前条件(规则)列表
* /sx reload   : 重新加载这个插件的配置

#### 权限(Permissions):

* 基本权限: sx-attribute.use
* 指令权限: sx-attribute.子指令 (例:sx-attribute.stats)

#### 配置 (Config): 

* [点击查看](./markdown/config.md)

#### 语言 (Messages): 

* [点击查看](./markdown/messages.md)

<br>

<a name="tags"></a>
## 标签介绍 Tags

#### 属性标签(Attribute): 

* 攻击力 - 基础伤害，包括弓1、剑等，支持最小-最大伤害 例: 攻击力: 200-500 [不支持负数]3
* PVP攻击力 - 只针对玩家的伤害，支持最小-最大PVP伤害 [不支持负数]
* PVE攻击力 - 只针对怪物的伤害，支持最小-最大PVE伤害 [不支持负数]
* 命中几率 - 抵消 闪避几率 的效果
* 破甲几率 - 无视对方防御力、反射、格挡等防御效果，造成真实伤害
* 暴击几率 - 增加玩家暴击几率
* 暴击伤害 - 增加暴击所造成的伤害
* 吸血几率 - 增加玩家的吸血几率
* 吸血倍率 - 触发吸血时回复部分 已造成2 的伤害血量
* 点燃几率 - 造成2-5秒的燃烧效果
* 凋零几率 - 造成2-5秒的1-2级凋零效果
* 中毒几率 - 造成2-5秒的1-2级中毒效果
* 失明几率 - 造成2-5秒的1-2级失明效果
* 缓慢几率 - 造成2-5秒的1-2级缓慢效果
* 雷霆几率 - 造成单独的0-10%当前生命值雷霆伤害
* 撕裂几率 - 造成3秒内不同频率的总共4-12%当前生命值撕裂伤害

<br>

* 生命上限 - 提高血量最大上限值
* 生命恢复 - 每秒恢复的血量值
* 防御力 - 基础防御，伤害值为: 攻击力-防御力，支持最小-最大防御 例: 防御力: 200-400 [不支持负数]3
* PVP防御力 - 只针对玩家的防御，支持最小-最大PVP防御 [不支持负数]
* PVE防御力 - 只针对怪物的防御，支持最小-最大PVE防御 [不支持负数]
* 闪避几率 - 一定几率完全闪避本次伤害，优先级大于 破甲效果 并且有向后闪避动作
* 韧性 - 抵消 点燃、凋零、点燃等几率效果
* 反射几率 - 一定几率将伤害反弹给攻击者 触发反射时，格挡无法生效
* 反射伤害 - 每次反射的 已造成 伤害百分比
* 格挡几率 - 一定几率格挡伤害 触发格挡时，反射无法生效
* 格挡伤害 - 每次格挡的 已造成 伤害百分比

<br>

* 经验加成 - 提高每次获得的经验值百分比
* 速度 - 提高移动速度

#### 规则标签(Condition): 

* 主手/副手 - 当装备lore中写了只支持主手时，装在副手无法使属性生效
* 攻击速度 - 主手武器属性 例: 攻击速度: +50% 根据不同武器的默认攻击速度而调整增幅
* 限制职业 - 需要一定权限才允许使用该物品，例: 限制职业: 法师  -  所需权限: sx-attribute.法师
* 限制等级 - 限制玩家的最低使用等级
* 到期时间 - 限制玩家的使用期限
* 售出价格 - 设置物品的售出价格，在/sx sell 界面中进行售出
* 耐久度 - SX-Attribute的耐久度可以用于所有物品。物品在有耐久条的情况下，可同步物品本身耐久条进度。

#### 统计标签 (非Lore标签):

* 战斗力 - 根据玩家当前属性换算成战斗力数值，可以在Config.yml的每个属性下方的Value中调整

#### 注释:

* 使用弓近战不会使弓的属性生效。
* 已造成的意思是攻击-暴击-破甲/防御-反射-格挡后剩余的伤害值。
* 除了攻击力、防御力不支持负数以外，其余都支持。在所有属性计算完毕后，最终属性都会进行自动纠正。

<br>

<a name="tutorial"></a>
## 详细教程 Tutorial

#### 随机字符串: [点击查看](./markdown/tutorial/random.md)

#### 物品配置: [点击查看](./markdown/tutorial/idm.md)

#### MythicMobs 怪物掉落: [点击查看](./markdown/tutorial/mmdrop.md)

#### MythicMobs 怪物装备: [点击查看](./markdown/tutorial/mmequip.md)

#### PlaceholderAPI 变量: [点击查看](./markdown/documents/papi.md)

<br>

<a name="downloads"></a>
## 下载链接 Downloads

* 插件下载: SX-Attribute -> [SX-Attribute-1.3.6.jar (241.2 KB, 下载记录: 1435 - 18/9/4 ](http://www.mcbbs.net/forum.php?mod=attachment&aid=MTIwNzg2M3w4M2QxNjA0Y3wxNTI5MzYxMTkxfDE1NjE4ODN8NzkzMzYy)
* 插件版本: V1.3.8 - 18/8/30 18:50
* 可选前置: [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) / [HolographicDisplays](https://dev.bukkit.org/projects/holographic-displays) / [MythicMobs](https://www.spigotmc.org/resources/%E2%9A%94-mythicmobs-%E2%96%BAthe-1-custom-mob-creator%E2%97%84.5702/) / [RPGInventory](https://www.spigotmc.org/resources/rpg-inventory-premium-now-without-bugs-d-1-7-10-1-12-x.12498/) / [SkillAPI](https://www.spigotmc.org/resources/skillapi.4824/)
* 插件作者: [Saukiya](https://github.com/Saukiya)
* 相关附属: 
  * [SX-Level](http://www.mcbbs.net/thread-801326-1-1.html) - 一个等级控制系统插件，可以根据权限控制玩家的最大等级
  * [SX-Resource](http://www.mcbbs.net/thread-810267-1-1.html) - RPG额外材质包，只需要高清修复mod，即可修改武器饰品外观
* 相关资料: 
  * [暗黑机制模板](http://www.mcbbs.net/thread-816644-1-1.html) - 参考暗黑破坏神装备生成机制编写的随机物品模板
<br>

<a name="development"></a>
## 开发文档 Development

* [开发总览](./markdown/api/overview.md)
* [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)
* [注册属性](./markdown/api/attribute.md)
* [注册条件](./markdown/api/condition.md)
* [API](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/api/SXAttributeAPI.html)


<br>

<a name="license"></a>
## 开源条款: [GNU/GPL v3](/LICENSE)

<br>
