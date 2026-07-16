package github.saukiya.sxattribute.feature.equipment;

import github.saukiya.sxattribute.feature.equipment.core.FeatureItemState;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;
import org.junit.Test;

/** 验证装备状态层不会把空槽位传入跨版本 NBT 适配器。 */
public class FeatureItemStateTest {

    @Test
    public void rejectsNullAndAirItems() {
        Assert.assertFalse(FeatureItemState.isUsable(null));
        Assert.assertFalse(FeatureItemState.isUsable(new ItemStack(Material.AIR)));
        Assert.assertTrue(FeatureItemState.isUsable(new ItemStack(Material.STONE)));
    }
}
