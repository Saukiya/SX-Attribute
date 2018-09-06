## SX-Attribute 开发 - 注册属性

 [开发总览](./overview.md)&nbsp;&nbsp;
 [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)&nbsp;&nbsp; 
 [注册属性](./attribute.md)&nbsp;&nbsp; 
 [注册条件](./condition.md)&nbsp;&nbsp; 
 [API](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/api/SXAttributeAPI.html)

<br>

SubAttribute
--

* 每个 [SubAttribute](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html) 都会成为 [SXAttributeData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeData.html) 的内部对象
* [SXAttributeData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeData.html) 交给 [SXAttributeManager](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/SXAttributeManager.java) 管理
* 注意，这不是属性管理器，只是一个单独的对象
* 比较特别的属性示范 - [MythicmobsDropAttribute](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/sub/other/MythicmobsDropAttribute.java)
* [Code](https://github.com/Saukiya/SX-Attribute/tree/master/src/main/java/github/saukiya/sxattribute/data/attribute)

属性实现
--

* 首先我们创建一个实现类，测试类为 `TestAttribute` ，然后让他继承 [SubAttribute](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html)，
* 默认需要编写一个构造器和五个方法，你也可以覆盖其他方法
* 下面我们进行详细的介绍

#### 属性构造器

* `SubAttribute(String name, int doublesLength, SXAttributeType... attributeTypes)`
* 构造器需要申明:
  * 属性名: 这个属性的简称
  * 属性长度: SubAttribute 内置了一个double数组，根据情况采用不同长度，例如暴击(几率/伤害)需要的长度为2
  * 属性类型: [SXAttributeType](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeType.html) 具体分为以下四种，可以给属性分配多个 SXAttributeType 用于不同事件
    * `SXAttributeType.DAMAGE` 攻击型属性，执行攻击方的 eventMethod - DamageEventData
    * `SXAttributeType.DEFENCE` 防御型属性，执行防御方的 eventMethod - DamageEventData
    * `SXAttributeType.UPDATE` 更新型属性，执行实体的 eventMethod - UpdateEventData
    * `SXAttributeType.OTHER` 自定义属性，可以不存数据，也不参与SX内部事件，例如 <abbr title="一个内部控制 Mythicmobs 掉落的特殊属性">[MythicmobsDropAttribute](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/sub/other/MythicmobsDropAttribute.java)</abbr>
* 构造器注意事项:
  * SXAttributeData 会通过 `class.newInstance()` 的方式实例化一个新属性对象
  * 所以属性构造器默认不能带成员变量 例如 `new TestAttribute() - 无参数`
  * 请在构造内部直接用 `super(name, length, attributeType);`

#### 需编写的五个方法

* `eventMethod(EventData eventData)` - 事件执行方法:
  * EventData 是个抽象类 分为 <a href="#damageeventdata">DamageEventData</a> 和 <a href="#updateeventdata">UpdateEventData</a>
* `getPlaceholder(Player player, String string)` - placeholder变量转换方法:
  * 检测string并提供相应的变量，无变量则返回null
* `getPlaceholders()` - 提供该属性placeholder列表:
  * 提供你当前的属性变量，可在 `/sx attributeList` 指令中显示
* `loadAttribute(String lore)` - 从lore中读取属性:
  * 判断lore是否为你插件的字符串，是则修改属性并存储
* `getValue()` - 将属性转为战斗点数:
  * 返回属性double数据所转为的战斗点数
  
#### 可覆盖的各个方法

* `onEnable()` - 属性注册后<abbr title="代表属性有优先级，并且没被其他属性覆盖">加载成功</abbr>时执行的启动方法
* `onDisable()` - SX关闭时执行属性的结束方法
* `correct()` - 纠正错误的属性，默认为每个属性的<abbr title="代表集合了装备、手持、自定义槽、RPGInventory(如果开启)、API的数据">最终数据</abbr>不得低于零
* `introduction()` - 简述这个属性的作用，可在 `/sx attributeList` 指令中显示

#### 不可覆盖的方法(部分)

* `getName()` - 获取属性名
* `getPlugin()` - 获取注册该 Attribute 的 JavaPlugin
* `registerAttribute(JavaPlugin plugin)` - 注册 Attribute，需要在插件的 `onLoad()` 方法中使用
* `getAttributes()` - 获取属性数组，可以直接修改数值

#### 静态工具类方法

* `getFirstPerson()` - 获取第一人称称呼，例如 "**你** 被 Saukiya 点燃了"
* `probability(double d)`- 判断触发几率 d 如果为100 那么触发几率为100%
* `getNumber(String lore)` - 获取lore中的有效数字
* `getDf(String lore)` - 获取lore中的有效数字(唔 非线程安全 目前未收到报错)

#### Attribute 优先级

* Attribute 的优先级统一在SX的Config中读取，位于 `AttributePriority.属性名`
* Attribute 的处理方式由 1 2 3 ... 根据TreeMap排序，数值越低优先处理
* 优先级为 -1 / 没有设置优先级时，禁用该属性
* SX更新版本后，会替换Config文件并备份

#### EventData子类

<a name="damageeventdata"></a>
##### *DamageEventData*

* `sendHolo(String message)` 添加一行全息信息，尽量控制在5个字以内
* `getEntityAttributeDoubles(String attributeName)` 获取防御方的属性值，不存在则返回 new double[12]
* `getDamagerAttributeDoubles(String attributeName)` 获取攻击方的属性值，不存在则返回 new double[12]
* `addDamage(double addDamage)` 增加伤害值
* `takeDamage(double takeDamage)` 减少伤害值
* `getEffectiveAttributeList()` 获取被触发的特殊属性列表，例如触发破甲后，导致防御属性失效
* `setCancelled(boolean cancelled)` 取消该事件 剩下的属性不会执行方法

<a name="updateeventdata"></a>
##### *UpdateEventData*

* `getEntity()` 获取执行该事件的实体


#### 具体代码示范
* ##### 属性实现类 - TestAttribute.java

```java
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.entity.Player;

import java.util.List;

public class TestAttribute extends SubAttribute {

    public TestAttribute() {
        super("TestAttribute", 2, SXAttributeType.DAMAGE);
    }
    
    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData){
            // Method
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Test1") ? getDf().format(getAttributes()[0]) : 
                string.equalsIgnoreCase("Test2") ? getDf().format(getAttributes()[1]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.as("Test1","Test2");//此处Placeholder调用方式为 %sx_Test1% %sx_Test2%
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(设定好的检测字符串1)) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
        }
        else if (lore.contains(设定好的检测字符串2)) {
            getAttributes()[1] += Double.valueOf(getNumber(lore));
        }
        else {
            return false;
        }
        return true;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * (设定好的转换值1)
                + getAttributes()[1] * (设定好的转换值2);
    }
    
}
```

* ##### 属性注册方法 - Plugin.java 

```java
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class Plugin extends JavaPlugin implements Listener{
    
    // 只允许在插件的onLoad方法中注册及调整属性
    @Override
    public void onLoad() {
        // 实例化一个属性并注册
        new TestAttribute().registerAttribute(this);
    }
    
}
```


#### 具体使用方法，可参考SX属性源码:   [Attribute](https://github.com/Saukiya/SX-Attribute/tree/master/src/main/java/github/saukiya/sxattribute/data/attribute)

<br>