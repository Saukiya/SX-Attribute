## SX-Attribute 开发 - 注册属性

 [开发总览](./overview.md)&nbsp;&nbsp;
 [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)&nbsp;&nbsp; 
 [注册属性](./attribute.md)&nbsp;&nbsp; 
 [注册条件](./condition.md)&nbsp;&nbsp; 
 [事件监听](./events.md)&nbsp;&nbsp; 
 [API](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/api/SXAttributeAPI.html)

<br>

### 介绍

属性


<br>

### 创建一个属性类
首先我们创建一个属性类，测试类为 `TestAttribute` ，然后让他继承 [SubAttribute](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html)，默认需要编写一个构造器和五个方法，你也可以覆盖其他方法

#### 属性构造器: 
`SubAttribute(String name, int doublesLength, SXAttributeType... attributeTypes)`

#### 需编写的五个方法:
* eventMethod(EventData eventData) - 事件执行方法:
  * EventData 是个抽象类 分为 DamageEventData 和 UpdateEventData
* getPlaceholder(Player player, String string) - placeholder变量转换方法:
  * 检测string并提供相应的变量，无变量则返回null
* getPlaceholders() - 提供该属性placeholder列表:
  * 提供你当前的属性变量，可以在/sx attributeList 指令中显示
* loadAttribute(String lore) - 从lore中读取属性:
  * 判断lore是否为你插件的字符串，是则修改属性并存储
* getValue() - 将属性转为战斗点数:
  * 返回属性double数据所转为的战斗点数

<br>

下面为示范:

```
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