## SX-Attribute 开发 - 注册属性

 [开发总览](./overview.md)&nbsp;&nbsp;
 [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)&nbsp;&nbsp; 
 [注册属性](./attribute.md)&nbsp;&nbsp; 
 [注册条件](./condition.md)&nbsp;&nbsp; 
 [事件监听](./events.md)&nbsp;&nbsp; 
 [API](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/api/SXAttributeAPI.html)

<br>

### 介绍

* 每个 [SubAttribute](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html) 都会成为 [SXAttributeData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeData.html) 的内部对象
* [SXAttributeData](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeData.html) 交给 [SXAttributeManager](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/SXAttributeManager.java) 管理
* 只需要把属性视为单独对象

<br>

### 创建一个属性类

* 首先我们创建一个属性类，测试类为 `TestAttribute` ，然后让他继承 [SubAttribute](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html)，
* 默认需要编写一个构造器和五个方法，你也可以覆盖其他方法
* 下面我们进行详细的介绍

#### 属性构造器

* `SubAttribute(String name, int doublesLength, SXAttributeType... attributeTypes)`
* 构造器需要申明:
  * 属性名: 这个属性的简称
  * 属性长度: 例如暴击(Crit) 内有暴击几率和暴击伤害，那么长度为2 可使用getAttributes()方法调出
  * 属性类型: [SXAttributeType](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SXAttributeType.html) 具体分为以下四种，可以给属性分配多个 SXAttributeType 用于不同事件
    * `SXAttributeType.DAMAGE` 攻击型属性，伤害事件中会执行攻击方的Attribute
    * `SXAttributeType.DEFENCE` 防御型属性，伤害事件中会执行防御方的Attribute
    * `SXAttributeType.UPDATE` 更新型属性，更新实体(或玩家)的各项数据
    * `SXAttributeType.OTHER` 自定义属性，可以不存数据，也不参与SX内部事件，例如 [MythicmobsDropAttribute](https://github.com/Saukiya/SX-Attribute/blob/master/src/main/java/github/saukiya/sxattribute/data/attribute/sub/other/MythicmobsDropAttribute.java)
    
#### 需编写的五个方法

* `eventMethod(EventData eventData)` - 事件执行方法:
  * EventData 是个抽象类 分为 DamageEventData 和 UpdateEventData
* `getPlaceholder(Player player, String string)` - placeholder变量转换方法:
  * 检测string并提供相应的变量，无变量则返回null
* `getPlaceholders()` - 提供该属性placeholder列表:
  * 提供你当前的属性变量，可在 `/sx attributeList` 指令中显示
* `loadAttribute(String lore)` - 从lore中读取属性:
  * 判断lore是否为你插件的字符串，是则修改属性并存储
* `getValue()` - 将属性转为战斗点数:
  * 返回属性double数据所转为的战斗点数
  
#### 可覆盖的各个方法

* onEnable() - 属性注册后<abbr title="代表属性有优先级，并且没被其他属性覆盖">加载成功</abbr>时执行的启动方法
* onDisable() - SX关闭时执行属性的结束方法
* correct() - 纠正错误的属性，默认为每个属性的**最终数据**不得低于零
* introduction() - 简述这个属性的作用，可在/sx attributeList 指令中显示

#### 不可覆盖的方法
* 

<br>

#### 下面具体代码示范
* ##### 属性实现类 - TestAttribute.java

```java
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.entity.Player;

import java.util.List;

public class TestAttribute extends SubAttribute {

    //实现类的构造器不能含有成员变量
    public TestAttribute() {
        //super(属性名, 属性长度, 属性类型 - 可多个);
        super("TestAttribute", 2, SXAttributeType.DAMAGE);
    }

    /**
     *事件执行方法
     *EventData可分为:
     *DamageEventData - 战斗型事件处理
     *UpdateEventData - 更新型事件处理
     *需要instanceof判断
     */
    @Override
    public void eventMethod(EventData eventData) {
        // Method
    }

    /**
     *提供的papi获取属性
     *判断成功则返回确切字符
     *失败则返回null
     */
    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Test1") ? getDf().format(getAttributes()[0]) : 
                string.equalsIgnoreCase("Test2") ? getDf().format(getAttributes()[1]) : null;
    }

    //提供变量列表 papi调用方式为 %sx_Test1% %sx_Test2%
    @Override
    public List<String> getPlaceholders() {
        return Arrays.as("Test1","Test2");
    }

    //从lore中加载属性
    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(设定好的检测字符串)) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
        }
        else if (lore.contains(设定好的检测字符串)) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
        }
        else {
            return false;
        }
        return true;
    }

    //属性转成战斗点数
    @Override
    public double getValue() {
        return getAttributes()[0] * (设定好的转换值)
                + getAttributes()[1] * (设定好的转换值);
    }
}
```
* ##### 属性注册方法 - Plugin.java 
```java
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin implements Listener{
    
    // 只允许在插件的onLoad方法中注册及调整属性
    @Override
    public void onLoad() {
        // 实例化一个属性并注册
        new TestAttribute().registerAttribute(this);
    }
    
}

```