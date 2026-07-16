package github.saukiya.sxattribute.feature.equipment;

import github.saukiya.sxattribute.feature.equipment.core.WeightedSelector;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Test;

/** 加权表边界测试。 */
public class WeightedSelectorTest {

    /** 唯一正权重节点必须稳定返回自身，零权重节点不得入选。 */
    @Test
    public void choosesOnlyPositiveWeightEntry() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("Only:\n  Weight: 1\nDisabled:\n  Weight: 0\n");
        for (int index = 0; index < 20; index++) {
            Assert.assertEquals("Only", WeightedSelector.choose(yaml));
        }
    }
}
