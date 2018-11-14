package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.ItemUtil;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Saukiya
 */
@Getter
public class RegisterSlot {

    private String name;

    private ItemStack item;

    private int slot;

    @SuppressWarnings("deprecation")
    public RegisterSlot(int slot, String name, String id, ItemUtil itemUtil) {
        this.slot = slot;
        this.name = name.replace("&", "§");
        int itemMaterial = 160, itemDurability = id == null ? 15 : 0;
        if (id != null) {
            if (id.contains(":")) {
                String[] idSplit = id.split(":");
                itemMaterial = Integer.valueOf(idSplit[0]);
                itemDurability = Integer.valueOf(idSplit[1]);
            } else {
                itemMaterial = Integer.valueOf(id);
            }
        }
        this.item = itemUtil.setNBT(new ItemStack(itemMaterial, 1, (short) itemDurability), "Slot", this.name);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        if (SXAttribute.getVersionSplit()[1] >= 11) {
            //1.11.2方法
            meta.setUnbreakable(true);
        } else {
            //1.9.0方法
            meta.spigot().setUnbreakable(true);
        }
        String itemName = Config.getConfig().getString(Config.REGISTER_SLOTS_LOCK_NAME);
        if (itemName != null) {
            meta.setDisplayName(itemName.replace("&", "§").replace("%SlotName%", this.name));
        }
        this.item.setItemMeta(meta);
    }

}
