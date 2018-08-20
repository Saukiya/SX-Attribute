package github.saukiya.sxattribute.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Saukiya
 */
class ItemData {

    @Getter
    private ItemStack item;

    @Getter
    private List<String> ids;

    @Getter
    private List<String> enchantList;

    ItemData(ItemStack item, List<String> ids, List<String> enchantList) {
        this.item = item;
        this.ids = ids;
        this.enchantList = enchantList;
    }
}
