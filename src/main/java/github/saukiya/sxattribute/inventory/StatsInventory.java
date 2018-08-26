package github.saukiya.sxattribute.inventory;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.MoneyUtil;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Saukiya
 * @since 2018年3月24日
 */

public class StatsInventory {

    @Getter
    private static final List<UUID> hideList = new ArrayList<>();

    private final SXAttribute plugin;

    public StatsInventory(SXAttribute plugin) {
        this.plugin = plugin;
    }

    public void openStatsInventory(Player player, Player... openInvPlayer) {
        SXAttributeData attributeData = plugin.getAttributeManager().getEntityData(player);
        Inventory inv = Bukkit.createInventory(null, 27, Message.getMsg(Message.INVENTORY__STATS__NAME));
        ItemStack stainedGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = stainedGlass.getItemMeta();
        glassMeta.setDisplayName("§c");
        stainedGlass.setItemMeta(glassMeta);
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta skullmeta = skull.getItemMeta();
        List<String> skullLoreList = new ArrayList<>();
        if (hideList.contains(player.getUniqueId())) {
            skullLoreList.add(Message.getMsg(Message.INVENTORY__STATS__HIDE_OFF));
        } else {
            skullLoreList.add(Message.getMsg(Message.INVENTORY__STATS__HIDE_ON));
        }
        skullLoreList.addAll(setPlaceholders(player, attributeData, Message.getStringList(Message.INVENTORY__STATS__SKULL_LORE)));
        if (SXAttribute.isPlaceholder()) {
            skullLoreList = PlaceholderAPI.setPlaceholders(player, skullLoreList);
        }
        skullmeta.setLore(skullLoreList);
        skullmeta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__SKULL_NAME, player.getDisplayName()));
        // 1.12.2 以上的方法
        if (SXAttribute.getVersionSplit()[1] >= 12) {
            new Thread(() -> ((SkullMeta) skullmeta).setOwningPlayer(player));
            skull.setItemMeta(skullmeta);
            inv.setItem(4, skull);
        } else if (SXAttribute.getVersionSplit()[1] >= 9) {
            ((SkullMeta) skullmeta).setOwner(player.getName());
        }
        skull.setItemMeta(skullmeta);
        for (int i = 0; i < 9; i++) {
            if (i == 4) {
                inv.setItem(i, skull);
            } else {
                inv.setItem(i, stainedGlass);
            }
        }
        for (int i = 18; i < 27; i++) {
            inv.setItem(i, stainedGlass);
        }
        inv.setItem(10, getAttackUI(player, attributeData));
        inv.setItem(13, getDefenseUI(player, attributeData));
        inv.setItem(16, getBaseUI(player, attributeData));
        if (openInvPlayer.length > 0) {
            openInvPlayer[0].openInventory(inv);
        } else {
            player.openInventory(inv);
        }
    }

    private ItemStack getAttackUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__ATTACK));
        List<String> loreList = setPlaceholders(player, data, Message.getStringList(Message.INVENTORY__STATS__ATTACK_LORE));
        if (SXAttribute.isPlaceholder()) {
            loreList = PlaceholderAPI.setPlaceholders(player, loreList);
        }
        if (!hideList.contains(player.getUniqueId())) {
            for (int i = loreList.size() - 1; i >= 0; i--) {
                if (Double.valueOf(SubAttribute.getNumber(loreList.get(i)).replace("-", "")) == 0D) {
                    loreList.remove(i);
                }
            }
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getDefenseUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__DEFENSE));
        List<String> loreList = setPlaceholders(player, data, Message.getStringList(Message.INVENTORY__STATS__DEFENSE_LORE));
        if (SXAttribute.isPlaceholder()) {
            loreList = PlaceholderAPI.setPlaceholders(player, loreList);
        }
        if (!hideList.contains(player.getUniqueId())) {
            for (int i = loreList.size() - 1; i >= 0; i--) {
                if (Double.valueOf(SubAttribute.getNumber(loreList.get(i)).replace("-", "")) == 0D) {
                    loreList.remove(i);
                }
            }
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getBaseUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__BASE));
        List<String> loreList = setPlaceholders(player, data, Message.getStringList(Message.INVENTORY__STATS__BASE_LORE));
        if (SXAttribute.isPlaceholder()) {
            loreList = PlaceholderAPI.setPlaceholders(player, loreList);
        }
        if (!hideList.contains(player.getUniqueId())) {
            for (int i = loreList.size() - 1; i >= 0; i--) {
                if (Double.valueOf(SubAttribute.getNumber(loreList.get(i)).replace("-", "")) == 0D) {
                    loreList.remove(i);
                }
            }
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> setPlaceholders(Player player, SXAttributeData data, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String lore = list.get(i);
            while (lore.contains("%") && lore.split("%").length > 1 && lore.split("%")[1].contains("sx_") && lore.split("%")[1].split("_").length > 1) {
                String[] loreSplit = lore.split("%");
                String string = loreSplit[1].split("_")[1];
                String str = null;
                if (string.equalsIgnoreCase("Money") && SXAttribute.isVault()) {
                    str = SXAttribute.getDf().format(MoneyUtil.get(player));
                } else if (string.equalsIgnoreCase("Health")) {
                    str = SXAttribute.getDf().format(player.getHealth());
                } else if (string.equalsIgnoreCase("value")) {
                    str = SXAttribute.getDf().format(data.getValue());
                } else {
                    for (SubAttribute attribute : data.getAttributeMap().values()) {
                        str = attribute.getPlaceholder(string);
                        if (str != null) break;
                    }
                }
                if (str != null) {
                    lore = lore.replaceFirst("%" + loreSplit[1] + "%", str);
                } else {
                    lore = lore.replaceFirst("%" + loreSplit[1] + "%", "N/A");
                }
            }
            list.set(i, lore);
        }
        return list;
    }
}
