package github.saukiya.sxattribute.data.itemdata;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.event.SXItemSpawnEvent;
import github.saukiya.sxattribute.event.SXItemUpdateEvent;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Saukiya
 */
public class ItemDataManager {
    @Getter
    private static final List<IGenerator> generators = new ArrayList<>();

    private final File itemFiles = new File(SXAttribute.getInst().getDataFolder(), "Item");

    private final Map<String, IGenerator> itemMap = new HashMap<>();

    public ItemDataManager() {
        SXAttribute.getInst().getLogger().info("Loaded " + generators.size() + " ItemGenerators");
        loadItemData();
    }

    /**
     * 注册物品生成器
     *
     * @param generator ItemGenerator
     */
    public static void registerGenerator(IGenerator generator) {
        if (generator.getPlugin() == null) {
            SXAttribute.getInst().getLogger().info("ItemGenerator >>  [NULL|" + generator.getClass().getSimpleName() + "] Null Plugin!");
            return;
        }
        if (generator.getType() == null || generators.stream().anyMatch((ig) -> ig.getType().equals(generator.getType()))) {
            SXAttribute.getInst().getLogger().warning("ItemGenerator >>  [" + generator.getPlugin().getName() + "|" + generator.getClass().getSimpleName() + "] Type Error!");
            return;
        }
        generators.add(generator);
        SXAttribute.getInst().getLogger().info("ItemGenerator >> Register [" + generator.getPlugin().getName() + "|" + generator.getClass().getSimpleName() + "] To Type " + generator.getType() + " !");
    }

    /**
     * 获取商品价格
     *
     * @param item ItemStack
     * @return double
     */
    public static double getSellValue(ItemStack item) {
        double sell = -0D;
        List<String> loreList = item.getItemMeta().getLore();
        for (String lore : loreList) {
            if (lore.contains(Config.getConfig().getString(Config.NAME_SELL))) {
                sell = SubAttribute.getNumber(lore);
            }
        }
        return sell;
    }

    /**
     * 读取物品数据
     */
    public void loadItemData() {
        itemMap.clear();
        if (!itemFiles.exists() || Objects.requireNonNull(itemFiles.listFiles()).length == 0) {
            SXAttribute.getInst().saveResource("Item/Default/Default.yml", true);
            SXAttribute.getInst().saveResource("Item/NoLoad/Default.yml", true);
        }
        loadItem(itemFiles);
        SXAttribute.getInst().getLogger().info("Loaded " + itemMap.size() + " Items");
    }

    /**
     * 获取物品编号列表
     *
     * @return Set
     */
    public Set<String> getItemList() {
        return itemMap.keySet();
    }

    /**
     * 遍历读取物品数据
     *
     * @param files File
     */
    private void loadItem(File files) {
        for (File file : Objects.requireNonNull(files.listFiles())) {
            if (file.isDirectory()) {
                if (!file.getName().equals("NoLoad")) {
                    loadItem(file);
                }
            } else {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                do_1:
                for (String key : yaml.getKeys(false)) {
                    if (itemMap.containsKey(key)) {
                        SXAttribute.getInst().getLogger().warning("Don't Repeat Item Name: " + file.getName() + File.separator + key + " !");
                        continue;
                    }
                    String pathName = getPathName(files);
                    String type = yaml.getString(key + ".Type", "SX");
                    for (IGenerator generator : generators) {
                        if (generator.getType().equals(type)) {
                            itemMap.put(key, generator.newGenerator(pathName, key, yaml.getConfigurationSection(key)));
                            continue do_1;
                        }
                    }
                    SXAttribute.getInst().getLogger().warning("Don't Item Type: " + file.getName() + File.separator + key + " - " + type + " !");
                }
            }
        }
    }

    /**
     * 获取路径简称
     *
     * @param file File
     * @return PathName
     */
    private String getPathName(File file) {
        return file.toString().replace("plugins" + File.separator + "SX-Attribute" + File.separator, "").replace(File.separator, ">");
    }

    /**
     * 获取物品
     *
     * @param itemName String
     * @param player   Player
     * @return ItemStack / null
     */
    public ItemStack getItem(String itemName, Player player) {
        IGenerator ig = itemMap.get(itemName);
        if (ig != null) {
            ItemStack item = ig.getItem(player);
            if (ig instanceof IUpdate) {
                SXAttribute.getNbtUtil().setNBT(item, SXAttribute.getInst().getName() + "-Name", ig.getKey());
                SXAttribute.getNbtUtil().setNBT(item, SXAttribute.getInst().getName() + "-HashCode", ((IUpdate) ig).updateCode());
            }
            SXItemSpawnEvent event = new SXItemSpawnEvent(player, ig, item);
            Bukkit.getPluginManager().callEvent(event);
            return event.getItem();
        }
        return null;
    }

    /**
     * 返回是否存在物品
     *
     * @param itemName String
     * @return boolean
     */
    public boolean hasItem(String itemName) {
        return itemMap.containsKey(itemName);
    }

    /**
     * 更新物品
     *
     * @param oldItem ItemStack
     * @param player  Player
     */
    public void updateItem(ItemStack oldItem, Player player) {
        String dataName;
        if (oldItem != null && (dataName = SXAttribute.getNbtUtil().getNBT(oldItem, SXAttribute.getInst().getName() + "-Name")) != null) {
            IGenerator ig = itemMap.get(dataName);
            if (ig instanceof IUpdate && ((IUpdate) ig).isUpdate() && SXAttribute.getNbtUtil().hasNBT(oldItem, SXAttribute.getInst().getName() + "-HashCode")) {
                int hashCode = Integer.valueOf(SXAttribute.getNbtUtil().getNBT(oldItem, SXAttribute.getInst().getName() + "-HashCode"));
                if (((IUpdate) ig).updateCode() != hashCode) {
                    ItemStack item = ig.getItem(player);
                    SXAttribute.getNbtUtil().setNBT(item, SXAttribute.getInst().getName() + "-Name", ig.getKey());
                    SXAttribute.getNbtUtil().setNBT(item, SXAttribute.getInst().getName() + "-HashCode", ((IUpdate) ig).updateCode());
                    SXItemUpdateEvent event = new SXItemUpdateEvent(player, ig, item, oldItem);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        oldItem.setType(event.getItem().getType());
                        if (event.getItem().getType().getMaxDurability() == 0) {
                            oldItem.setDurability(event.getItem().getDurability());
                        }
                        oldItem.setItemMeta(event.getItem().getItemMeta());
                    }
                }
            }
        }
    }

    /**
     * 保存物品
     *
     * @param key  编号
     * @param item 物品
     * @param type 类型
     * @return boolean
     * @throws IOException IOException
     */
    public boolean saveItem(String key, ItemStack item, String type) throws IOException {
        for (IGenerator ig : generators) {
            if (ig.getType().equals(type)) {
                ConfigurationSection config = new MemoryConfiguration();
                config.set("Type", ig.getType());
                config = ig.saveItem(item, config);
                if (config != null) {
                    File file = new File(itemFiles, "Type-" + ig.getType() + File.separator + "Item.yml");
                    YamlConfiguration yaml = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
                    yaml.set(key, config);
                    yaml.save(file);
                    itemMap.put(key, ig.newGenerator(getPathName(file.getParentFile()), key, config));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 发送物品列表给指令者
     *
     * @param sender CommandSender
     * @param search String
     */
    public void sendItemMapToPlayer(CommandSender sender, String... search) {
        sender.sendMessage("");
        int filterSize = 0, size = 0;
        if (search.length > 0 && search[0].equals("")) {
            Message.Tool.sendTextComponent(sender, Message.Tool.getTextComponent("§eDirectoryList§8 - §7ClickOpen", "/sxattribute give |", "§8§o§lTo ItemList"));

            Map<String, String> map = new HashMap<>();
            for (IGenerator ig : itemMap.values()) {
                String str = map.computeIfAbsent(ig.getPathName(), k -> "");
                map.put(ig.getPathName(), str + "§b" + (str.replaceAll("[^\n]", "").length() + 1) + " - §a" + ig.getKey() + " §8[§7" + ig.getName() + "§8]§7 - §8[§cType:" + ig.getType() + "§8]\n");
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey(), value = entry.getValue(), command = "/sxattribute give |" + key + "<";
                TextComponent tc = Message.Tool.getTextComponent(" §8[§c" + key.replace(">", "§b>§c") + "§8]", command, null);
                tc.addExtra(Message.Tool.getTextComponent("§7 - Has §c" + value.split("\n").length + "§7 Item", command, value.substring(0, value.length() - 1)));
                Message.Tool.sendTextComponent(sender, tc);
            }
        } else {
            Message.Tool.sendTextComponent(sender, Message.Tool.getTextComponent("§eItemList§8 - " + (sender instanceof Player ? "§7ClickGet" + (search.length > 0 && search[0].matches("\\|.+<") ? " §8[§c" + search[0].substring(1, search[0].length() - 1).replace(">", "§b>§c") + "§8]" : "") : ""), "/sxattribute give", "§8§o§lTo DirectoryList"));
            for (IGenerator ig : itemMap.values()) {
                String itemName = ig.getName();
                String str = " §b" + (size + 1) + " - §a" + ig.getKey() + " §8[§7" + itemName + "§8]§7 - §8[§cType:" + ig.getType() + "§8]";
                if (search.length > 0 && !(str + "|" + ig.getPathName() + "<").contains(search[0])) {
                    filterSize++;
                    continue;
                }
                size++;
                if (sender instanceof Player) {
                    YamlConfiguration yaml = new YamlConfiguration();
                    ig.getConfig().getValues(false).forEach(yaml::set);
                    Message.Tool.sendTextComponent(sender, Message.Tool.getTextComponent(str, "/sxattribute give " + ig.getKey(), "§7" + yaml.saveToString() + "§8§o§lPath: " + ig.getPathName()));
                } else {
                    sender.sendMessage(str);
                }
            }
            if (search.length > 0 && filterSize != 0) {
                sender.sendMessage("§7> Filter§c " + filterSize + " §7Items, Left §c" + size + "§7 Items.");
            }
        }
        sender.sendMessage("");
    }
}