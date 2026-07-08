package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.util.AttributeConfig;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.Placeholders;
import github.saukiya.tools.nms.ItemUtil;
import github.saukiya.tools.util.ReMaterial;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 查询属性指令
 *
 * @author Saukiya
 */
public class StatsCommand extends SubCommand implements Listener {

    private static final InventoryHolder holder = () -> null;

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
        Inventory inv = Bukkit.createInventory(holder, 27, Message.getMsg(Message.INVENTORY__STATS__NAME));
        ItemStack stainedGlass = ReMaterial.BLACK_STAINED_GLASS_PANE.item();
        ItemMeta glassMeta = stainedGlass.getItemMeta();
        glassMeta.setDisplayName("§c");
        stainedGlass.setItemMeta(glassMeta);
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
        ItemStack skull = ReMaterial.PLAYER_HEAD.item();
        ItemMeta skullMeta = skull.getItemMeta();
        skullMeta.setLore(skullLoreList);
        skullMeta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__SKULL_NAME, player.getDisplayName()));
        if (Config.isCommandStatsDisplaySkullSkin()) {
            ItemUtil.getInst().setSkull(skullMeta, player.getName());
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
        inv.setItem(12, getMovementUI(player, attributeData));
        inv.setItem(13, getDefenseUI(player, attributeData));
        inv.setItem(14, getGatherUI(player, attributeData));
        inv.setItem(16, getBaseUI(player, attributeData));
        (openInvPlayer.length > 0 ? openInvPlayer[0] : player).openInventory(inv);
    }

    private ItemStack getAttackUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__ATTACK));
        List<String> loreList = process(player, data, panelLore("ATTACK", Message.INVENTORY__STATS__ATTACK_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getDefenseUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__DEFENSE));
        List<String> loreList = process(player, data, panelLore("DEFENSE", Message.INVENTORY__STATS__DEFENSE_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getBaseUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__BASE));
        List<String> loreList = process(player, data, panelLore("OTHER", Message.INVENTORY__STATS__BASE_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getMovementUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.IRON_BOOTS);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__MOVEMENT));
        List<String> loreList = process(player, data, panelLore("MOVEMENT", Message.INVENTORY__STATS__MOVEMENT_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getGatherUI(Player player, SXAttributeData data) {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Message.getMsg(Message.INVENTORY__STATS__GATHER));
        List<String> loreList = process(player, data, panelLore("GATHER", Message.INVENTORY__STATS__GATHER_LORE));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 取某分区面板 lore 源: AutoPanel 开则由 Attributes.yml 的 Display 自动生成, 否则回退 Message.yml。
     *
     * @param category 分区 (ATTACK/DEFENSE/MOVEMENT/GATHER/OTHER)
     * @param fallback AutoPanel 关闭时的 Message.yml 键
     * @return lore 行 (含 %sx_xxx% 占位, 交由 process 渲染)
     */
    private List<String> panelLore(String category, Message fallback) {
        return AttributeConfig.isAutoPanel() ? buildDisplayLore(category) : Message.getStringList(fallback);
    }

    /**
     * 按 Attributes.yml 各属性节点的 Display 元数据, 生成指定分区的面板 lore 行。
     * <p>
     * 收集所有 {@code Display.Category} 匹配的属性, 按 {@code Display.Order} 升序, 逐 Row 生成:
     * 含 {@code line} 则原样使用; 否则拼 {@code {color}{label}:&b %sx_{placeholder}%{suffix}}。
     *
     * @param category 目标分区
     * @return 未渲染的 lore 行列表 (可变)
     */
    private List<String> buildDisplayLore(String category) {
        TreeMap<Integer, List<String>> ordered = new TreeMap<>();
        int autoOrder = 100000;
        for (String name : AttributeConfig.attributeNames()) {
            ConfigurationSection sec = AttributeConfig.getSection(name);
            if (sec == null) {
                continue;
            }
            ConfigurationSection display = sec.getConfigurationSection("Display");
            if (display == null || !category.equalsIgnoreCase(display.getString("Category", ""))) {
                continue;
            }
            List<String> lines = new ArrayList<>();
            for (Map<?, ?> row : display.getMapList("Rows")) {
                Object line = row.get("line");
                if (line != null) {
                    // 与 Message.yml 路径一致: & 颜色码转 §, 供 process 渲染与隐藏 0 值判断
                    lines.add(String.valueOf(line).replace('&', '§'));
                    continue;
                }
                Object placeholder = row.get("placeholder");
                if (placeholder == null) {
                    continue;
                }
                String color = row.get("color") == null ? "" : String.valueOf(row.get("color"));
                String label = row.get("label") == null ? "" : String.valueOf(row.get("label"));
                String suffix = row.get("suffix") == null ? "" : String.valueOf(row.get("suffix"));
                lines.add((color + label + ":&b %sx_" + placeholder + "%" + suffix).replace('&', '§'));
            }
            if (!lines.isEmpty()) {
                ordered.computeIfAbsent(display.getInt("Order", autoOrder++), k -> new ArrayList<>()).addAll(lines);
            }
        }
        List<String> result = new ArrayList<>();
        for (List<String> value : ordered.values()) {
            result.addAll(value);
        }
        return result;
    }

    private List<String> process(Player player, SXAttributeData data, List<String> list) {
        // getStringList 在 key 缺失时会返回不可变的 singletonList (如旧 Message.yml 未含新分区键),
        // 而下方 set/remove 需要可变列表, 故统一包装为 ArrayList 防止 UnsupportedOperationException
        list = new ArrayList<>(list);
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
        if (!event.isCancelled() && holder.equals(event.getInventory().getHolder())) {
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
