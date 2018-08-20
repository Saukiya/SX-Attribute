## SX-Attribute 教程 - MythicMobs 怪物掉落

<br>

#### 掉落格式: - 'sx \<物品编号\> \[物品数量\] \[掉落概率\]'

#### 怪物配置例子:

```yml
SkeletalKnight:
  Type: WITHER_SKELETON
  Display: 'SkeletalKnight'
  Health: 100
  Damage: 8
  Drops:
  - sx 默认一 5-50 0.5       #50% 几率掉落 5-50 个 默认一 物品
  - sx 默认二                #掉落 1 个 默认二 物品
  - sx 默认三 1-2            #掉落 1-2 个 默认三 物品
  - sx 默认四 1 0.001        #0.1% 几率掉落 1 个 默认四 物品
  Options:
    MovementSpeed: 0.5
```

#### *如果物品编号填写错误，后台会有信息警告:*

```log
[13:34:11 INFO]: [SX-Attribute] Mythicmobs怪物: SkeletalKnight 不存在这个掉落物品: 默认三
[13:34:20 INFO]: [SX-Attribute] Mythicmobs怪物: SkeletalKnight 不存在这个掉落物品: 默认三
>mm reload
[13:34:26 INFO]: MythicMobs has been reloaded!
[13:34:29 INFO]: [SX-Attribute] Mythicmobs怪物: SkeletalKnight 不存在这个掉落物品: 默认三
[13:34:29 INFO]: [SX-Attribute] Mythicmobs怪物: SkeletalKnight 不存在这个掉落物品: 默认四
[13:34:32 INFO]: [SX-Attribute] Mythicmobs怪物: SkeletalKnight 不存在这个掉落物品: 默认三
[13:34:39 INFO]: [SX-Attribute] Mythicmobs怪物: SkeletalKnight 不存在这个掉落物品: 默认四
```

#### 怪物掉落截图:

![screenshot](https://i.loli.net/2018/06/06/5b179396ef89d.jpg)


#### [返回主页](../../README.md)
