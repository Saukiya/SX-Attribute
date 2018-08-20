## SX-Attribute - 配置文件 (config.yml)

<br>

```yml
#配置文件版本
ConfigVersion: 1.1.0
#是否启用物品自动更新
#开启后，可以根据物品编号即时更新玩家手中的物品 (保持耐久度进度条)
#注意！使用随机字符串的情况下，请不要开启此项！
ItemUpdate:
  Enabled: false
#是否启用全息伤害显示
#消息文本可在 Message.yml 中修改
Holographic:
  Enabled: true
  #停留时间 (单位:Tick)
  DisplayTime: 40
#血量显示相关
Health:
  #是否启用头顶显血
  NameVisible:
    Enabled: true
    #血条总长度
    Size: 10
    #当前血量每格显示的文本
    Current: '[爱心]'
    #已损血量每格显示文本
    Loss: '&7[爱心]'
    #显血前缀
    Prefix: '&8[&c'
    #显血后缀
    Suffix: '&8]'
    #停留时间
    DisplayTime: 40
  #是否启用BossBar显血
  BossBar:
    Enabled: true
    # {0} - 生物名称 (可在Message.yml 的最底下进行中英替换)
    # {1} - 生物当前血量
    # {2} - 生物最大血量
    Format: '&a&l{0}:&8&l[&a&l{1}&7&l/&c&l{2}&8&l]'
    # 停留时间
    DisplayTime: 100
#是否启用血条压缩功能
HealthScaled:
  Enabled: true
  #血量压缩锁定值 每2个Value算一颗心
  #例如我当前最大生命值是1000，他只会显示两排
  #最大血量低于Value数值的情况下，自动不压缩
  Value: 40
#是否启用物品显示名字 (只显示有DisplayName的物品)
ItemDisplayName: true
#是否处理非玩家的属性计算 （指双方都不是玩家的情况）
DamageCalculationToEVE: false
#是否开启1.9战斗模式（十分推荐）
DamageGauges: true
#是否禁用盾牌右键（不禁用可能会造成伤害免疫效果）
BanShieldInteract: false
#是否清除物品原版标签（例如默认的攻击力、护甲，会导致伤害不准确）
ClearDefaultAttribute:
  #本插件产生的物品
  ThisPlugin: true
  #所有 工具类 的物品 （开启此项后 村民交易的工具物品 可能会出问题 需要手动更新）
  All: false
  #出现上述问题，关闭以上选项，开启此项，即可恢复原版标签。
  Reset: false
#启用RPGInventory时默认开启功能
RPGInventory:
  #GUI内不读取物品属性的位置
  #5号位置为个人信息区域，禁止读取
  #12号位为手持区域，如果读取则会导致手持属性变化
  #如果以上两区域经过个人修改被变动，请同时修改如下列表
  WhiteSlot:
  - 5
  - 12
#是否开启随机字符串
RandomString: true
#修复物品 -> 每一点破损度所需的金币
RepairItemValue: 3.5
#属性相关:
#Name: 识别Lore中的属性文本 可以是 “攻击力: +50” 也可以是 “+50 攻击力”
#限制职业只能是 ”限制职业:剑士“ 或 ”限制职业：剑士“
#Value: 换算成战斗力的点数
#例子: (Value都为1时）
#例子一  “暴击几率: +50%” -> 战斗力+50
#例子二 “攻击力: +30-50” -> 战斗力+(50+30)/2=40
Stats:
  #Hand-识别方式:
  #当在主手物品上识别到"副武器"时，不读取该物品属性，并提示消息，反之同理
  Hand:
    InMain:
      Name: 主武器
    InOff:
      Name: 副武器
  #Armor-识别方式:
  #当在主/副手物品上识别到以下列表字符串时，不读取该物品属性，不提示消息
  Armor:
  - 头盔
  - 盔甲
  - 护腿
  - 靴子
  Role:
    Name: 限制职业
  LimitLevel:
    Name: 限制等级
  ExpAddition:
    Name: 经验加成
    Value: 1
  Durability:
    Name: 耐久度
  Sell:
    Name: 售出价格
  ExpiryTime:
    Name: 到期时间
    #这里是时间格式
    #yyyy/年  MM/月 dd/日 HH/小时 mm/分钟
    #例子: "到期时间: 2018/07/28 16:00"
    #如果Format为 yyyy/MM/dd
    #那么应该填写: "到期时间: 2018/07/28"
    Format: yyyy/MM/dd HH:mm
  Speed:
    Name: 速度
    Value: 1
  AttackSpeed:
    Name: 攻击速度
  Health:
    Name: 生命上限
    Value: 1
  HealthRegen:
    Name: 生命恢复
    Value: 1
  Dodge:
    Name: 闪避几率
    Value: 1
  Defense:
    Name: 防御力
    Value: 1
  PVPDefense:
    Name: PVP防御力
    Value: 1
  PVEDefense:
    Name: PVE防御力
    Value: 1
  Toughness:
    Name: 韧性
    Value: 1
  ReflectionRate:
    Name: 反射几率
    Value: 1
  Reflection:
    Name: 反射伤害
    Value: 1
  BlockRate:
    Name: 格挡几率
    Value: 1
  Block:
    Name: 格挡伤害
    Value: 1
  Damage:
    Name: 攻击力
    Value: 1
  PVEDamage:
    Name: PVP攻击力
    Value: 1
  PVPDamage:
    Name: PVE攻击力
    Value: 1
  HitRate:
    Name: 命中几率
    Value: 1
  Real:
    Name: 破甲几率
    Value: 1
  Crit:
    Name: 暴击几率
    Value: 1
  CritDamage:
    Name: 暴击伤害
    Value: 1
  LifeStealRate:
    Name: 吸血几率
    Value: 1
  LifeSteal:
    Name: 吸血倍率
    Value: 1
  Ignition:
    Name: 点燃几率
    Value: 1
  Wither:
    Name: 凋零几率
    Value: 1
  Poison:
    Name: 中毒几率
    Value: 1
  Blindness:
    Name: 失明几率
    Value: 1
  Slowness:
    Name: 缓慢几率
    Value: 1
  Lightning:
    Name: 雷霆几率
    Value: 1
  Tearing:
    Name: 撕裂几率
    Value: 1
```


#### [返回主页](../README.md)
