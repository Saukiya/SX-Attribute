package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.attribute.AttributeSource;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 属性源统计面板: 每个命名源一格, 展示其贡献的非零属性
 *
 * @author Ray_Hughes
 */
public class StatsSourceCommand extends SubCommand implements Listener {

    private static final InventoryHolder holder = () -> null;

    public StatsSourceCommand() {
        super("statssource");
        setArg(" [Player]");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_CONSOLE));
            return;
        }
        Player target = SourceCommand.resolveTarget(sender, args);
        if (target == null) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
            return;
        }
        List<String> names = new ArrayList<>(SXAttribute.getAttributeManager().getSourceNames(target.getUniqueId()));
        SXAttributeData defaultData = SXAttribute.getAttributeManager().getDefaultAttributeData();
        boolean hasDefault = defaultData != null && defaultData.isValid();
        int total = names.size() + (hasDefault ? 1 : 0);
        int size = Math.max(9, Math.min(54, (total + 8) / 9 * 9));
        Inventory inv = Bukkit.createInventory(holder, size, "§d§l属性源统计 §7- §f" + target.getName());
        int slot = 0;
        for (String name : names) {
            if (slot >= size) {
                break;
            }
            AttributeSource source = SXAttribute.getAttributeManager().getSource(target.getUniqueId(), name);
            if (source == null) {
                continue;
            }
            inv.setItem(slot++, buildItem("§b§l" + name + (source.isSilent() ? " §8(静态)" : ""), source.getData()));
        }
        // 默认属性作为独立一格
        if (hasDefault && slot < size) {
            inv.setItem(slot, buildItem("§b§l默认 §8(全局)", defaultData));
        }
        ((Player) sender).openInventory(inv);
    }

    private static ItemStack buildItem(String name, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(name);
        List<String> lore = SourceCommand.describe(data);
        meta.setLore(lore.isEmpty() ? java.util.Collections.singletonList("§7(空)") : lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    void onInventoryClickStatsSourceEvent(InventoryClickEvent event) {
        if (holder.equals(event.getInventory().getHolder())) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.contains(args[1])).collect(Collectors.toList());
        }
        return null;
    }
}
