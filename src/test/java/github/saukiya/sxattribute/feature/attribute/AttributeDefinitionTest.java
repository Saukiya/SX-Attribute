package github.saukiya.sxattribute.feature.attribute;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Test;

/** 配置化属性值协议的纯配置测试。 */
public class AttributeDefinitionTest {

    /** 验证 Lore 匹配、求和与上下限纠正共享同一字段定义。 */
    @Test
    public void parsesAndAggregatesNamedValue() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("Priority: 10\nValues:\n  damage:\n    Match: 火元素伤害\n    Aggregate: SUM\n    Min: 0\n    Max: 100\n");
        AttributeDefinition definition = AttributeDefinition.parse("Fire", yaml);
        AttributeDefinition.ValueDefinition value = definition.getValues().get("damage");
        Assert.assertTrue(value.matches("§c火元素伤害: 12"));
        Assert.assertEquals(17D, value.aggregate(5D, 12D), 0D);
        Assert.assertEquals(100D, value.correct(200D), 0D);
    }
}
