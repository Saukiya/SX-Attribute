package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.Placeholders;
import github.saukiya.sxattribute.verision.MaterialControl;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 查询属性指令
 *
 * @author Saukiya
 */
public class StatsCommand extends SubCommand implements Listener {

    @Getter
    private final List<UUID> hideList = new ArrayList<>();

    public StatsCommand() {
        super("stats");
        setType(SenderType.PLAYER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length > 1 && sender.hasPermission(SXAttribute.getInst().getName() + ".admin")) {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player != null) {
                openStatsInventory(player, (Player) sender);
            } else {
                sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
            }
        }
        openStatsInventory((Player) sender);
    }

    public void openStatsInventory(Player player, Player... openInvPlayer) {
        SXAttributeData attributeData = SXAttribute.getApi().getEntityData(player);
        Inventory inv = Bukkit.createInventory(null, 27, Message.getMsg(Message.INVENTORY__STATS__NAME));
        ItemStack stainedGlass = MaterialControl.BLACK_STAINED_GLASS_PANE.parseItem();
        ItemMeta glassMeta = stainedGlass.getItemMeta();
        glassMeta.setDisplayName("§c");
        stainedGlass.setItemMeta(glassMeta);
        ItemStack skull = MaterialControl.PLAYER_HEAD.parseItem();
        ItemMeta skullMeta = skull.getItemMeta();
        List<String> skullLoreList = new ArrayList<>();
        if (hideList.contains(player.getUniqueId())) {
            skullLoreList.add(Message.getMsg(Message.INVENTORY__STATS__HIDE_OFF));
        } else {
            skullLoreList.add(Message.getMsg(Message.INVENTORY__STATS__HIDE_ON));
        }
        skullLoreList.addAll(process(player, attributeData, Message.getStringList(Message.INVENTORY__STATS__SKULL_LORE)));
        if (SXAttribute.isPlaceholder()) {
            skullLoreList = PlaceholderAPI.setPlaceholders(player, skullLoreList);
        }
        skullMeta.setLore(skullLoreList);
        skullMeta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__SKULL_NAME, player.getDisplayName()));
        if (Config.isCommandStatsDisplaySkullSkin()) {
            ((SkullMeta) skullMeta).setOwner(player.getName());
        }
        skull.setItemMeta(skullMeta);
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
        (openInvPlayer.length > 0 ? openInvPlayer[0] : player).openInventory(inv);
    }

    private ItemStack getAttackUI(Player player, SXAttributeData data) {
        ItemStack item = MaterialControl.DIAMOND_SWORD.parseItem();
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__ATTACK));
        List<String> loreList = process(player, data, Message.getStringList(Message.INVENTORY__STATS__ATTACK_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getDefenseUI(Player player, SXAttributeData data) {
        ItemStack item = MaterialControl.DIAMOND_CHESTPLATE.parseItem();
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__DEFENSE));
        List<String> loreList = process(player, data, Message.getStringList(Message.INVENTORY__STATS__DEFENSE_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getBaseUI(Player player, SXAttributeData data) {
        ItemStack item = MaterialControl.BOOK.parseItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__BASE));
        List<String> loreList = process(player, data, Message.getStringList(Message.INVENTORY__STATS__BASE_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> process(Player player, SXAttributeData data, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String lore = list.get(i);
            while (lore.contains("%") && lore.split("%").length > 1 && lore.split("%")[1].contains("sx_") && lore.split("%")[1].split("_").length > 1) {
                String[] loreSplit = lore.split("%");
                String str = Placeholders.onPlaceholderRequest(player, loreSplit[1].replaceFirst("sx_", ""), data);

                if (str != null) {
                    lore = lore.replaceFirst("%" + loreSplit[1] + "%", str);
                } else {
                    lore = lore.replaceFirst("%" + loreSplit[1] + "%", "N/A");
                }
            }
            list.set(i, lore);
        }
        if (SXAttribute.isPlaceholder()) {
            list = PlaceholderAPI.setPlaceholders(player, list);
        }
        if (!hideList.contains(player.getUniqueId())) {
            for (int i = list.size() - 1; i >= 0; i--) {
                String lore = list.get(i).replaceAll("§+[0-9]", "");
                if (lore.replaceAll("[^1-9]", "").length() == 0 && lore.replaceAll("[^0-9]", "").length() > 0) {
                    list.remove(i);
                }
            }
        }
        return list;
    }


    @EventHandler
    void onInventoryClickStatsEvent(InventoryClickEvent event) {
        if (!event.isCancelled() && Message.getMsg(Message.INVENTORY__STATS__NAME).equals(event.getInventory().getName())) {
            if (event.getRawSlot() < 0) {
                event.getView().getPlayer().closeInventory();
                return;
            }
            event.setCancelled(true);
            if (event.getRawSlot() == 4) {
                Player player = (Player) event.getView().getPlayer();
                if (getHideList().contains(player.getUniqueId())) {
                    getHideList().remove(player.getUniqueId());
                } else {
                    getHideList().add(player.getUniqueId());
                }
                openStatsInventory(player);
            }
        }
    }
}
