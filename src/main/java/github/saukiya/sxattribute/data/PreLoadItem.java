package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.data.condition.EquipmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.inventory.ItemStack;

/**
 * 预加载的物品
 *
 * @author Saukiya
 */

@AllArgsConstructor
@Getter
@ToString
public class PreLoadItem {

    private EquipmentType type;

    private ItemStack item;

    public PreLoadItem(ItemStack item) {
        this.type = EquipmentType.ALL;
        this.item = item;
    }

}
