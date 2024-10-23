package github.saukiya.sxattribute.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public enum ReMaterial {
    WHITE_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 0),
    YELLOW_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 4),
    GRAY_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 7),
    BLUE_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 9),
    BLACK_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 15),
    PLAYER_HEAD("SKULL_ITEM", 0),
    ;

    private Material material;
    private short subId;

    ReMaterial(String legacyName, int subId) {
        material = Material.getMaterial(legacyName);
        if (material != null) {
            this.subId = (short) subId;
        } else {
            material = Material.getMaterial(name());
        }
    }

    public ItemStack item() {
        return new ItemStack(material, 1, subId);
    }

    public void setType(ItemStack item) {
        item.setType(material);
        item.setDurability(subId);
    }
}
