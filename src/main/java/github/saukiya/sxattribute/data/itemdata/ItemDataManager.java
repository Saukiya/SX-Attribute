package github.saukiya.sxattribute.data.itemdata;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxitem.data.item.ItemManager;

/**
 * @deprecated
 */
public class ItemDataManager extends ItemManager {
    public ItemDataManager() {
        super(SXAttribute.getInst(), "SXAttribute-Name", "SXAttribute-HashCode", "SXAttribute");

    }
}
