## SX-Attribute 教程 - 物品配置

<br>

#### 字段说明:

|  字段名  |  字段介绍  |
| :------------: | :------------ |
|  Name  |  设置物品名称  支持RandomString  |
|  ID  |  设置物品ID  支持RandomString  支持列表随机式输入  |
|  Lore  |  设置物品Lore  支持RandomString  支持PAPI变量  |
|  EnchantList  |  设置物品附魔<br>格式: "<附魔英文>:<附魔等级>"<br>[附魔英文ID文档](https://docs.windit.net/Chinese_BukkitAPI/org/bukkit/enchantments/Enchantment.html)  |
|  ItemFlagList  |  设置物品隐藏选项<br>格式: "\<ItemFlag英文\>"<br>[相关文档](https://docs.windit.net/Chinese_BukkitAPI/org/bukkit/inventory/ItemFlag.html)  |
|  Unbreakable  |  设置物品是否为无限耐久  |
|  Color  |  设置皮革物品的颜色<br>格式: "R,G,B"  |
|  SkullName  |  设置玩家头颅的展示ID  |

#### 例子:

```yml
配置示范-头颅:
  Name: <s:DefaultPrefix> &c我只是个示范头颅 <s:DefaultSuffix>
  ID: '397:3'
  Lore:
  - '&6品质等级: <s:<l:品质>Color><l:品质>'
  - '&6限制等级: <s:<l:品质>等级-10>级'
  - '&c防御力: +20'
  - '&r'
  - '<s:DefaultLore>'
  EnchantList:
  - DURABILITY:5
  ItemFlagList:
  - HIDE_ENCHANTS
  - HIDE_UNBREAKABLE
  SkullName: Notch
  Unbreakable: true
配置示范-皮革:
  Name: <s:DefaultPrefix> &c我只是个示范皮革 <s:DefaultSuffix>
  ID: 
  - '29<r:8_9>'
  - '30<r:0_1>'
  Lore:
  - '&6品质等级: <s:<l:品质>Color><l:品质>'
  - '&6限制等级: <s:<l:品质>等级-10>级'
  - '&c防御力: +<r:20_50>'
  - '&r'
  - '<s:DefaultLore>'
  EnchantList:
  - DURABILITY:5
  - PROTECTION_ENVIRONMENTAL:3
  ItemFlagList:
  - HIDE_UNBREAKABLE
  Color: 123,111,126
  Unbreakable: true
```

#### 例子效果截图:

![screenshot](https://i.loli.net/2018/06/06/5b17cf26e0186.jpg)

#### 搜索关键词功能:

* 搜索格式: '/sx give <关键词>'
* ![screenshot](https://i.loli.net/2018/06/06/5b17cf97001aa.jpg)

#### [返回主页](../../README.md)
