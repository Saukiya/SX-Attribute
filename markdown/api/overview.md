## SX-Attribute 开发 - 开发总览

 [开发总览](./overview.md)&nbsp;&nbsp;
 [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)&nbsp;&nbsp; 
 [注册属性](./attribute.md)&nbsp;&nbsp; 
 [注册条件](./condition.md)&nbsp;&nbsp; 
 [API](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/api/SXAttributeAPI.html)

<br>

### 基础介绍

* 属性管理器 - [SXAttributeManager](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeManager.html)([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/SXAttributeManager.java))
  * 属性数据 - [SXAttributeData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeData.html)([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/SXAttributeData.java))
    * 属性标签 - [SubAttribute](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html)([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/SubAttribute.java))
      * 属性类型枚举 - *[SXAttributeType](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeType.html)*([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/SXAttributeType.java))
      * 属性执行事件 - <abbr title="伤害事件">[DamageEventData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/eventdata/sub/DamageEventData.html)</abbr>([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/eventdata/sub/DamageEventData.java)) / <abbr title="更新事件">[UpdateEventData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/eventdata/sub/UpdateEventData.html)</abbr>([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/eventdata/sub/UpdateEventData.java))
* 条件管理器 - [SXConditionManager](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/condition/SXConditionManager.html)([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/condition/SXConditionManager.java))
  * 条件标签 - [SubCondition](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/condition/SubCondition.html)([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/condition/SubCondition.java))
    * 条件类型枚举 - *[SXConditionType](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/condition/SXConditionType.html)*([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/condition/SXConditionType.java))
* 属性更新事件 - [UpdateStatsEvent](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/event/UpdateStatsEvent.html)([Code](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/event/UpdateStatsEvent.java))

<br>

### 属性读取过程

* 玩家物品更新时，SX会分开读取手持、装备、自定义槽、RPGInventory数据，并分别保存。以下介绍过程：
* 执行[getItemData(LivingEntity entity, SXConditionType type, ItemStack... itemArray)](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeManager.html#getItemData-org.bukkit.entity.LivingEntity-github.saukiya.sxattribute.data.condition.SXConditionType-org.bukkit.inventory.ItemStack...-)方法：
* 新建一个**总数据** [SXAttributeData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeData.html) ，为每个物品创建一个 AttributeData 并遍历物品lore。根据优先级，先遍历[条件标签](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/condition/SubCondition.html)、再遍历[属性标签](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html)。
* 执行[条件标签](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/condition/SubCondition.html)的方法 [determine(LivingEntity entity, ItemStack item, String lore)](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/condition/SubCondition.html#determine-org.bukkit.entity.LivingEntity-org.bukkit.inventory.ItemStack-java.lang.String-) 会返回如下枚举：
  * `SXConditionReturnType.LORE` 跳过当行lore属性读取，进行下一行lore识别
  * `SXConditionReturnType.ITEM` 该物品不符合要求，SXAttributeData 为 null
  * `SXConditionReturnType.NULL` 判断通过，进行下一个条件判断
* 条件标签判断结束后，并且都为NULL或者空指针，那么遍历属性标签。
* 执行属性标签的方法 [loadAttribute(String lore)](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html#loadAttribute-java.lang.String-) 会返回如下boolean:
  * `true` 当前lore符合你的属性读取，读取完毕后。修改当前AttributeData为**有效**属性，并进行下一行lore识别
  * `false` 当前lore不符合你的属性读取。进行其他属性标签的识别
* 当物品Lore遍历完后，当AttributeData为**有效**时，添加到总数据中，并使总数据为**有效**，然后遍历下一个物品Lore。
* 当全部物品遍历完后，则执行事件[UpdateStatsEvent](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/event/UpdateStatsEvent.html)，注意事项:
  * 事件中的 SXAttributeData 初始值可以是 null，原因在于没有得到有效的属性，或者条件判断物品不符合要求
  *事件中的 ItemList 的内值可以是 null，条件判断该物品不符合要求时，数组相应位置则设为 null
  * 这个事件是异步的
  * 事件执行结束后，若 SXAttribute 不为null 那么存储数据

<br>

### 攻击事件过程

* 某些原因导致SX的处理事件优先级为 `EventPriority.HIGHEST(仅次于MONITOR)`
* 不处理 `DamageCause.CUSTOM` 类型的伤害事件
* 不处理防御方为 `ArmorStand(盔甲架)` 的伤害事件
* 当攻击方为抛射物时 读取抛射物属性: 
  * 在射箭后 SX会将攻击方当前的属性绑定到抛射物中
  * 防止攻击方射箭后切换成其他武器，造成微量BUG
  * 攻击方会调整为抛射物的所有者
* 当一切数值获取正常后，根据优先级同时遍历双方的 AttributeData，并根据属性类型分配方法
* 参与方法的只有两种类型: `攻击-SXAttributeType.DAMAGE / 防御-SXAttributeType.DEFENCE`
* 为攻击方分配`DAMAGE`类型属性方法，为防御方分配`DEFENCE`类型属性方法
* 当`DamageEventData` 被取消，或者`damageEventData.getDamage() <= 0`时，退出属性遍历，并设置伤害为0.1D
* 无话可嗦，具体操作可看DamageEventData的JavaDoc