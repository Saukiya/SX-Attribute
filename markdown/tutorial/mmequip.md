## SX-Attribute 教程 - MythicMobs 怪物装备

<br>

#### 穿戴填写格式: - 'sx \<物品编号\>:\<穿戴位置\> \[穿戴概率\]'

##### *注意: 本插件暂不干扰MM怪的生命值修正，以后可能会提供设置*

#### 怪物配置示范:

```yml
SkeletalKnight:
  Type: WITHER_SKELETON
  Display: 'SkeletalKnight'
  Health: 100
  Damage: 8
  Equipment:
  - KingsCrown:4          #MM原版:头上戴个金帽子
  - sx 默认一:0            #在手上设置一个 默认一 物品
  - sx 默认二:1 0.5        #50%几率在脚上设置一个 默认二 物品
```

#### 穿戴位置介绍:

* -1 是 副手物品 注: 部分怪物会出现无法显示的现象
* 0  是 主手物品
* 1  是 鞋子
* 2  是 裤子
* 3  是 衣服
* 4  是 帽子

#### 怪物穿戴截图: (*因为默认一就是随机ID 所以就是这个效果*)

![ss](https://i.loli.net/2018/06/06/5b179f4dd4497.jpg)


#### [返回主页](../../README.md)
