## SX-Attribute - 消息配置 (messages.yml)

<br>

```yml
#配置文件版本
MessageVersion: 1.1.0
#与玩家有关的消息
#都可以在修改发送方式
#例如 [TITLE] [ACTIONBAR] 消息
#不写前缀则是普通消息框消息
Player:
  NoLevelUse: '&8[&dSX-Attribute&8] &c你没有达到使用 &a{0} &c的等级要求!'
  NoRole: '&8[&dSX-Attribute&8] &c你没有达到使用 &a{0} &c的职业要求!'
  NoHand: '&8[&dSX-Attribute&8] &7物品 &a{0} &7只适合装在 &a{1}.'
  OverdueItem: '&8[&dSX-Attribute&8] &c物品 &a{0}&c 已经过期了!'
  ExpAddition: '&8[&dSX-Attribute&8] &7你的经验额外增加了 &6{0}&7! [&a{1}%&7]'
  Battle:
    Crit: '[ACTIONBAR]&c{0}&6 对 &c{1}&6 造成了暴击!'
    Ignition: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 点燃了!'
    Wither: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 凋零了!'
    Poison: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 中毒了!'
    Blindness: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 致盲了!'
    Slowness: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 减速了!'
    Lightning: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 用雷电击中了!'
    Real: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 破甲了!'
    Tearing: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 撕裂了!'
    Reflection: '[ACTIONBAR]&c{0}&6 被 &c{1}&6 反弹伤害了!'
    Block: '[ACTIONBAR]&c{0}&6 格挡了 &c{1}&6 的部分伤害!'
    Dodge: '[ACTIONBAR]&c{0}&6 躲开了 &c{1}&6 的攻击!'
    NullDamage: '[ACTIONBAR]&c{0}&6 无法对 &c{1}&6 造成伤害!'
  #如果删除其中的某一条特效，那么不会在触发特效的时候提示该特效信息
  #例如删除 Real破甲整行(注意是一整行)，那么在触发破甲的情况下，不会提示消息
  Holographic:
    Crit: '&a&o暴击: &b&o+{0}'
    Ignition: '&c&o点燃: &b&o{0}s'
    Wither: '&7&o凋零: &b&o{0}s'
    Poison: '&5&o中毒: &b&o{0}s'
    Blindness: '&8&o致盲: &b&o{0}s'
    Slowness: '&b&o减速: &b&o{0}s'
    Lightning: '&e&o雷霆'
    Real: '&c&o破甲'
    Tearing: '&c&o撕裂'
    Reflection: '&6&o反伤: &b&o{0}%'
    Block: '&2&o格挡: &b&o{0}%'
    Dodge: '&a&o闪避'
    LifeSteal: '&c&o吸取: &b&o{0}'
    NullDamage: '&c&o无伤'
    Damage: '&c&o伤害: &b&o{0}'
Inventory:
  #/sx stats 界面文本配置
  #支持Placeholder变量
  #预设变量无需Placeholder也可生效 (先进行内部处理)
  Stats:
    Name: '&d&l&oSX-Attribute'
    HideOn: '&a点击显示更多属性'
    HideOff: '&c点击隐藏更多属性'
    SkullName: '&6&l&o{0} 的属性'
    SkullLore:
    - '&d战斗力:&b %sx_value%'
    Attack: '&a&l&o攻击属性'
    AttackLore:
    - '&c攻击力:&b %sx_damage%'
    - '&cPVP攻击力:&b %sx_pvpdamage%'
    - '&cPVE攻击力:&b %sx_pvedamage%'
    - '&a命中几率:&b %sx_hitRate%%'
    - '&6破甲几率:&b %sx_real%%'
    - '&c暴击几率:&b %sx_crit%%'
    - '&4暴击伤害:&b %sx_critDamage%%'
    - '&6吸血几率:&b %sx_lifeStealRate%%'
    - '&6吸血倍率:&b %sx_lifeSteal%%'
    - '&c点燃几率:&b %sx_ignition%%'
    - '&9凋零几率:&b %sx_wither%%'
    - '&d中毒几率:&b %sx_poison%%'
    - '&3失明几率:&b %sx_blindness%%'
    - '&3缓慢几率:&b %sx_slowness%%'
    - '&e雷霆几率:&b %sx_lightning%%'
    - '&c撕裂几率:&b %sx_tearing%%'
    Defense: '&9&l&o防御属性'
    DefenseLore:
    - '&6防御力:&b %sx_defense%'
    - '&6PVP防御力:&b %sx_pvpdefense%'
    - '&6PVE防御力:&b %sx_pvedefense%'
    - '&a生命上限:&b %sx_health%'
    - '&a生命恢复:&b %sx_healthRegen%'
    - '&d闪避几率:&b %sx_dodge%%'
    - '&9韧性:&b %sx_toughness%%'
    - '&c反射几率:&b %sx_reflectionRate%%'
    - '&c反射伤害:&b %sx_reflection%%'
    - '&2格挡几率:&b %sx_blockRate%%'
    - '&2格挡伤害:&b %sx_block%%'
    Base: '&9&l&o其他属性'
    BaseeLore:
    - '&e经验加成:&b %sx_expAddition%%'
    - '&b速度:&b %sx_speed%%'
  Sell:
    Name: '&6&l售出物品'
    Sell: '&e&l点击售出'
    Enter: '&c&l确认售出'
    Out: '&6售出完毕:&e {0} 金币'
    NoSell: '&c&l不可售出'
    Lore:
      Default:
      - '&7&o请放入你要售出的物品'
      Format: '&b[{0}] &a{1}&7 - &7{2}&e 金币'
      NoSell: '&b[{0}] &4不可售出'
      AllSell: '&e总金额: {0}'
  Repair:
    Name: '&9&l修理物品'
    Guide: '&7&o待修理物品放入凹槽'
    Enter:
      Name: '&e&l点击修理'
      Lore:
      - '&7&o价格: {0}/破损值'
    Money:
      Name: '&c&l确认修理'
      'No': '&c&l金额不足'
      Lore:
      - '&c破损值: {0} 耐久'
      - '&e价格: {1} 金币'
      - '&7&o价格: {2}/破损值'
    Unsuited: '&4&l不可修理'
    Repair:
      Name: '&6修理成功:&e {0} 金币'
Admin:
  ClearEntityData: '&8[&dSX-Attribute&8] &c清理了 &6{0}&c 个多余的生物属性数据!'
  NoItem: '&8[&dSX-Attribute&8] &c物品不存在!'
  HasItem: '&8[&dSX-Attribute&8] &c已经存在名字为  &6{0}&c的物品!'
  GiveItem: '&8[&dSX-Attribute&8] &c给予 &6{0} &a{1}&c个 &6{2}&c 物品!'
  SaveItem: '&8[&dSX-Attribute&8] &a物品 &6{0} &a成功保存! 编号为: &6{1}&a!'
  NoPermissionCommand: '&8[&dSX-Attribute&8] &c你没有权限执行此指令'
  NoCommand: '&8[&dSX-Attribute&8] &c未找到此子指令:{0}'
  NoFormat: '&8[&dSX-Attribute&8] &c格式错误!'
  NoOnline: '&8[&dSX-Attribute&8] &c玩家不在线或玩家不存在!'
  NoConsole: '&8[&dSX-Attribute&8] &c控制台不允许执行此指令!'
  PluginReload: '&8[&dSX-Attribute&8] §c插件已重载'
Command:
  stats: 查看属性
  sell: 打开售出界面
  repair: 打开修理界面
  give: 给予玩家RPG物品
  save: 保存当前的物品到配置文件
  reload: 重新加载这个插件的配置
#替换原版生物的英文区 按照格式增加即可，适用于BossBar 和 Player.Battle 区域
ReplaceList:
  Pig: 猪猪
  Sheep: 羊羊
  Rabbit: 兔兔
  Mule: 骡骡
  Skeleton: 骷髅
  Zombie: 僵尸
  Silverfish: 蠢虫
  Horse: 马马
  Cow: 牛牛
  Chicken: 鸡鸡
```


#### [返回主页](../README.md)
