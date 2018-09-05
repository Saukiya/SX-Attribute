## SX-Attribute 开发 - 注册属性

 [开发总览](./overview.md)&nbsp;&nbsp;
 [JavaDoc](https://saukiya.github.io/SX-Attribute/javadoc/index.html)&nbsp;&nbsp; 
 [注册属性](./attribute.md)&nbsp;&nbsp; 
 [注册条件](./condition.md)&nbsp;&nbsp; 
 [事件监听](./events.md)&nbsp;&nbsp; 
 [API](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/api/SXAttributeAPI.html)

<br>

### 创建一个属性类
首先我们创建一个属性类，测试类为 TestAttribute，然后让他继承 [SubAttribute](https://saukiya.github.io/SX-Attribute/javadoc/github/saukiya/sxattribute/data/attribute/SubAttribute.html) 
*     import github.saukiya.sxattribute.data.attribute.SXAttributeType;
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
           *提供的papi获取属性的方法
           *判断成功则返回确切字符
           *失败则返回null
           */
          @Override
          public String getPlaceholder(Player player, String string) {
              return string.equalsIgnoreCase("Test1") ? getDf().format(getAttributes()[0]) : 
                      string.equalsIgnoreCase("Test2") ? getDf().format(getAttributes()[1]) : null;
          }
      
          //提供变量列表
          @Override
          public List<String> getPlaceholders() {
              return Arrays.as("Test1","Test2");
          }
      
          //从lore中加载属性的方法
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
      
          //属性转成战斗点数的方法
          @Override
          public double getValue() {
              return getAttributes()[0] * (设定好的转换值)
                      + getAttributes()[1] * (设定好的转换值);
          }
      }
然后喵喵喵喵喵