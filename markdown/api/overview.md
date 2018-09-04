## SX-Attribute 开发 - 开发总览

<br>
<br>
 
### 目录

* [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)
* [注册属性](./attribute.md)
* [注册条件](./condition.md)
* [事件监听](./events.md)
* [API使用](./api.md)
 
 ### 属性读取
 
 ```flow
 st=>start: 用户登陆
 op=>operation: 登陆操作
 cond=>condition: 登陆成功 Yes or No?
 e=>end: 进入后台
 
 st->op->cond
 cond(yes)->e
 cond(no)->op
 ```
 
* 玩家物品更新时，SX会分开读取手持、装备、自定义槽、RPGInventory。
* 然后执行[getItemData(LivingEntity entity, SXConditionType type, ItemStack... itemArray)](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeManager.html#getItemData-org.bukkit.entity.LivingEntity-github.saukiya.sxattribute.data.condition.SXConditionType-org.bukkit.inventory.ItemStack...-)方法：
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
  * 事件中的SXAttributeData、ItemList初始值可以是空指针
  * 这个事件是异步的