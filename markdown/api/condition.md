## SX-Attribute 开发 - 注册条件

 [开发总览](./overview.md)&nbsp;&nbsp;
 [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)&nbsp;&nbsp; 
 [注册属性](./attribute.md)&nbsp;&nbsp; 
 [注册条件](./condition.md)&nbsp;&nbsp; 
 [API](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/api/SXAttributeAPI.html)

<br>

SubCondition 介绍
--

* [SubCondition](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/condition/SubCondition.java) 只是单个对象，存储于 [SXConditionManager](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/condition/SXConditionManager.java) 并管理
* 比较特殊的 Condition 标签 - [DurabilityCondition](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/condition/sub/DurabilityCondition.java)
* SubCondition 不存储于 SXAttributeData
* [Code](https://github.com/Saukiya/SX-Attribute/tree/master/src/main/java/github/saukiya/sxattribute/data/condition)

SubCondition 实现
--

* 首先我们创建一个实现类，测试类为 `TestCondition` ，然后让他继承 [SubCondition](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/condition/SubCondition.html)，
* 默认需要编写一个构造器和一个方法，你也可以覆盖其他三个方法
* 下面我们进行详细的介绍&nbsp;&nbsp;~~人生赢家复读机~~

#### 条件构造器

* `SubCondition(String name, SXConditionType... type)`
* `SubCondition(String name)`
* 构造器需要申明:
  * 条件名: 这个条件的简称
  * 条件类型: [SXConditionType] 具体分为七种，不填则默认 `SXConditionType.ALL`
    * `SXConditionType.RPG_INVENTORY ` RPGInventory类型
    * `SXConditionType.EQUIPMENT ` 装备类型
    * `SXConditionType.HAND ` 手持类型
    * `SXConditionType.MAIN_HAND ` 主手类型 属于手持类型
    * `SXConditionType.OFF_HAND ` 副手类型 属于手持类型
    * `SXConditionType.SLOT ` 自定义槽类型
    * `SXConditionType.ALL ` 非指向类型，检测所有类型的物品，例如:限制等级

#### 需编写的一个方法
* `determine(LivingEntity entity, ItemStack item, String lore)` 判断该lore是否符合使用要求
  * item 可以是空指针
  * 返回类型为SXConditionReturnType，各个作用为:
    * `SXConditionReturnType.LORE` 跳过当行lore属性读取，进行下一行lore识别
    * `SXConditionReturnType.ITEM` 该物品不符合要求，SXAttributeData 为 null
    * `SXConditionReturnType.NULL` 判断通过，进行下一个 Condition 判断，返回 null 与该效果相同

 #### 可覆盖的各个方法

* `onEnable()` - Condition 注册后<abbr title="代表Condition有优先级，并且没被其他Condition覆盖">加载成功</abbr>时执行的启动方法
* `onDisable()` - SX 关闭时执行 Condition 的结束方法
* `introduction()` - 简述这个 Condition 的作用，可在 `/sx conditionList` 指令中显示

#### 不可覆盖的方法(部分)

* `getName()` - 获取条件名
* `getPlugin()` - 获取注册该 Condition 的 JavaPlugin
* `registerCondition(JavaPlugin plugin)` - 注册 Condition，需要在插件的 `onLoad()` 方法中使用

#### 静态工具类方法
* `getItemName(ItemStack item)` 获取物品名称，item为空指针时显示 "N/A"
* `getItemLevel(ItemStack item)` 获取物品等级 没有则为 -1
* `getLevel(LivingEntity entity)` 获取实体等级 怪物默认为10000
* `getNumber(String lore)` 获取lore中的有效数字
* `getDurability(String lore)` 分割 "/" 获取当前耐久值
* `getMaxDurability(String lore)` 分割 "/" 获取最大耐久值
* `getUnbreakable(ItemMeta meta)` 获取物品是否为无限耐久

#### Condition 优先级

* Condition 的优先级统一在SX的Config中读取，位于 `ConditionPriority.条件名`
* Condition 的处理方式由 1 2 3 ... 根据TreeMap排序，数值越低优先处理
* 优先级为 -1 / 没有设置优先级时，禁用该 Condition
* SX更新版本后，会替换Config文件并备份

#### 具体代码示范
* ##### 条件实现类 - TestCondition.java

```java
package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class TestCondition extends SubCondition {

    public TestCondition() {
        super("Test",SXConditionType.HAND);
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (判断lore是否符合条件){
            if (item != null && entity != null){ /* entity.sendMessage(getItemName(item) + " No Use"); */}
            return SXConditionReturnType.ITEM;
        }
        return SXConditionReturnType.NULL;
    }
    
}
```

* ##### 条件注册方法 - Plugin.java 

```java
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class Plugin extends JavaPlugin implements Listener{
    
    // 只允许在插件的onLoad方法中注册及调整条件
    @Override
    public void onLoad() {
        // 实例化一个条件并注册
        new TestCondition().registerCondition(this);
    }
    
}
```


#### 具体使用方法，可参考SX条件源码:   [Condition](https://github.com/Saukiya/SX-Attribute/tree/master/src/main/java/github/saukiya/sxattribute/data/condition)

<br>