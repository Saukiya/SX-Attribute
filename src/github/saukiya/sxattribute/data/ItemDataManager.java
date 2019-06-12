package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.data.condition.sub.DurabilityCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.ItemUtil;
import github.saukiya.sxattribute.util.Message;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Saukiya
 */
public class ItemDataManager {
    private static final List<String> COLOR_LIST = Arrays.asList("§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9");
    private static final List<String> COLOR_REPLACE_LIST = Arrays.asList("%零%", "%一%", "%二%", "%三%", "%四%", "%五%", "%六%", "%七%", "%八%", "%九%");

    private final File itemFiles = new File(SXAttribute.getPluginFile(), "Item");
    private final File itemDefaultFile = new File(itemFiles, "Default" + File.separator + "Default.yml");
    private final File itemImportFile = new File(itemFiles, "ImportItem.yml");
    private final Map<String, ItemData> itemMap = new HashMap<>();
    private final SXAttribute plugin;

    public ItemDataManager(SXAttribute plugin) throws IOException, InvalidConfigurationException {
        this.plugin = plugin;
        loadItemData();
    }

    /**
     * 清除物品颜色
     *
     * @param lore String
     * @return String
     */
    public static String clearColor(String lore) {
        for (int i = 0; i < 10; i++) {
            lore = lore.replace(COLOR_LIST.get(i), COLOR_REPLACE_LIST.get(i));
        }
        return lore;
    }

    /**
     * 恢复物品颜色
     *
     * @param lore String
     * @return String
     */
    public static String replaceColor(String lore) {
        for (int i = 0; i < 10; i++) {
            lore = lore.replace(COLOR_REPLACE_LIST.get(i), COLOR_LIST.get(i));
        }
        return lore;
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
                sell = Double.valueOf(SubAttribute.getNumber(lore));
            }
        }
        return sell;
    }

    /**
     * 读取物品数据
     *
     * @throws IOException                   IOException
     * @throws InvalidConfigurationException InvalidConfigurationException
     */
    public void loadItemData() throws IOException, InvalidConfigurationException {
        loadItemMap();
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + itemMap.size() + " §rItems");
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
     * 读取物品数据
     *
     * @throws IOException                   IOException
     * @throws InvalidConfigurationException InvalidConfigurationException
     */
    private void loadItemMap() throws IOException, InvalidConfigurationException {
        itemMap.clear();
        if (!itemFiles.exists() || Objects.requireNonNull(itemFiles.listFiles()).length == 0) {
            createDefaultItemData();
        }
        loadItem(itemFiles);
    }

    /**
     * 遍历读取物品数据
     *
     * @param files File
     * @throws IOException                   IOException
     * @throws InvalidConfigurationException InvalidConfigurationException
     */
    private void loadItem(File files) throws IOException, InvalidConfigurationException {
        for (File file : Objects.requireNonNull(files.listFiles())) {
            if (file.isDirectory()) {
                loadItem(file);
            } else if (!file.getName().equals(itemImportFile.getName())) {
                YamlConfiguration itemYml = new YamlConfiguration();
                itemYml.load(file);
                for (String name : itemYml.getKeys(false)) {
                    if (itemMap.containsKey(name)) {
                        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cDon't Repeat Item Name: §4" + file.getName() + File.separator + name + " §c!");
                        continue;
                    }
                    String itemName = itemYml.getString(name + ".Name");
                    List<String> ids = new ArrayList<>();
                    Object idObject = itemYml.get(name + ".ID");
                    if (idObject instanceof List) {
                        ids = itemYml.getStringList(name + ".ID");
                    } else {
                        ids.add(itemYml.getString(name + ".ID", "260"));
                    }
                    if (ids.size() == 0) {
                        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cItem: §4" + name + " §cNull ID!");
                        ids.add("260");
                    }
                    String id = plugin.getRandomStringManager().processRandomString(itemName, ids.get(SXAttribute.getRandom().nextInt(ids.size())), new HashMap<>());
                    List<String> itemLore = itemYml.getStringList(name + ".Lore");
                    List<String> enchantList = itemYml.getStringList(name + ".EnchantList");
                    List<String> itemFlagList = itemYml.getStringList(name + ".ItemFlagList");
                    Boolean unbreakable = itemYml.getBoolean(name + ".Unbreakable");
                    String colorStr = itemYml.getString(name + ".Color");
                    String skullName = itemYml.getString(name + ".SkullName");

                    ItemStack item = plugin.getItemUtil().getItemStack(itemName, id, itemLore, itemFlagList, unbreakable, colorStr, skullName);
                    if (item == null) {
                        continue;
                    }
                    int hashCode = item.getType().name().hashCode() / 100 + item.getDurability();

                    if (itemName != null) {
                        hashCode += itemName.hashCode() / 100;
                    }

                    if (itemLore != null) {
                        hashCode += itemLore.hashCode() / 100;
                    }

                    if (item.getEnchantments().size() > 0) {
                        hashCode += enchantList.hashCode() / 100;
                    }

                    if (item.getItemMeta().getItemFlags().size() > 0) {
                        hashCode += item.getItemMeta().getItemFlags().hashCode() / 100;
                    }

                    if (colorStr != null) {
                        Color color = Color.fromRGB(Integer.valueOf(colorStr.split(",")[0]), Integer.valueOf(colorStr.split(",")[1]), Integer.valueOf(colorStr.split(",")[2]));
                        hashCode += color.hashCode() / 100;
                    }

                    // 记录物品名 HashCode
                    if (Config.isClearDefaultAttributePlugin() && item.getItemMeta().hasLore()) {
                        plugin.getItemUtil().clearAttribute(item);
                    }

                    if (hashCode != item.getType().name().hashCode() / 100 + item.getDurability()) {
                        item = plugin.getItemUtil().setNBT(plugin.getItemUtil().setNBT(item, "Name", name), "HasCode", String.valueOf(hashCode));
                    }

                    itemMap.put(name, new ItemData(name, item, false, ids, enchantList, hashCode));
                }
            } else {
                YamlConfiguration itemYml = new YamlConfiguration();
                itemYml.load(file);
                for (String name : itemYml.getKeys(false)) {
                    if (itemMap.containsKey(name)) {
                        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cDon't Repeat Item Name: §4" + file.getName() + File.separator + name + " §c!");
                        continue;
                    }
                    ItemStack item = itemYml.getItemStack(name);
                    if (item != null) {
                        itemMap.put(name, new ItemData(name, item, true, Collections.singletonList(String.valueOf(item.getTypeId())), null, 0));
                    }
                }
            }
        }
    }

    /**
     * 创建物品默认数据
     *
     * @throws IOException IOException
     */
    private void createDefaultItemData() throws IOException {
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cCreate Item/Default.yml");
        YamlConfiguration itemData = new YamlConfiguration();
        itemData.set("默认一.Name", "<s:DefaultPrefix>&c炎之洗礼<s:DefaultSuffix> <s:<l:品质>Color><l:品质>");
        itemData.set("默认一.ID", Collections.singletonList("<s:<l:职业>ID>"));
        itemData.set("默认一.Lore", Arrays.asList("&6品质等级: <s:<l:品质>Color><l:品质>",
                "<s:<l:品质>职业>",
                "&6物品类型: 主武器",
                "&6限制等级: <s:<l:品质>等级-10>级",
                "&c攻击力: +<s:<l:品质>攻击-10>",
                "<s:<l:品质>攻一-10>",
                "<s:<l:品质>攻二-10>",
                "<s:<l:品质>攻三-10>",
                "<l:材质>",
                "<s:<l:品质>宝石孔>",
                "&7耐久度: <r:100_<s:<l:品质>耐久最低>>/<s:<l:品质>耐久>",
                "<s:<l:品质>绑定>",
                "&a获得时间: <t:0>",
                "&a到期时间: <t:600>"));
        itemData.set("默认一.EnchantList", Collections.singletonList("<s:<l:职业>附魔>"));
        itemData.set("默认一.Unbreakable", false);
        itemData.set("默认二.Name", "&c机械轻羽之靴");
        itemData.set("默认二.ID", 301);
        itemData.set("默认二.Lore", Arrays.asList("&6物品类型: 靴子", "&b防御力: +15", "&c生命上限: +2000", "&d移动速度: +100%", "&d闪避几率: +20%", "&2生命恢复: +10", "&e经验加成: +20%", "&6限制等级: <r:50_100>级", "&r", "<s:DefaultLore>", "&r", "&e出售价格: 250"));
        itemData.set("默认二.EnchantList", Collections.singletonList("DURABILITY:1"));
        itemData.set("默认二.ItemFlagList", Arrays.asList("HIDE_ENCHANTS", "HIDE_UNBREAKABLE"));
        itemData.set("默认二.Unbreakable", true);
        itemData.set("默认二.Color", "128,128,128");
        itemData.set("默认三.Name", "&b雷霆领主项链");
        itemData.set("默认三.ID", 287);
        itemData.set("默认三.Lore", Arrays.asList("&6物品类型: 项链",
                "&c生命上限: +200",
                "&d移动速度: +50%",
                "&d雷霆几率: +20%",
                "&6限制等级: <r:20_30>级",
                "&r", "&e出售价格: 500"));
        itemData.set("默认三.EnchantList", Collections.singletonList("DURABILITY:1"));
        itemData.set("默认三.ItemFlagList", Collections.singletonList("HIDE_ENCHANTS"));
        itemData.save(itemDefaultFile);
    }

    /**
     * 判断物品是否存在
     *
     * @param itemName String
     * @return boolean
     */
    public boolean isItem(String itemName) {
        return itemMap.containsKey(itemName);
    }

    /**
     * 获取物品
     *
     * @param itemName String
     * @param player  Player
     * @return ItemStack
     */
    @SuppressWarnings("deprecation")
    public ItemStack getItem(String itemName, Player player) {
        ItemData itemData = itemMap.get(itemName);
        if (itemData != null) {
            return itemData.getItem(plugin,player);
        } else {
            return null;
        }
    }

    /**
     * 更新物品
     *
     * @param item   ItemStack
     * @param player Player
     */
    public void updateItem(ItemStack item, Player player) {
        //判断物品是否有这个nbt
        if (item != null && plugin.getItemUtil().isNBT(item, "Name")) {
            String dataName = plugin.getItemUtil().getNBT(item, "Name");
            // 判断配置内的名称是否相同
            ItemData itemData = itemMap.get(dataName);
            if (itemData != null && !itemData.isImportItem()) {
                // 获取物品的HashCode
                int hasCode = Integer.valueOf(Objects.requireNonNull(plugin.getItemUtil().getNBT(item, "HasCode")));
                ItemStack dataItem = itemData.getItem(plugin, player);
                assert dataItem != null;
                ItemMeta dataMeta = dataItem.getItemMeta();
                // 获取ItemMap物品的HashCode
                int dataHasCode = itemData.getHashCode();
                // 如果两者的原始Lore数据相同
                if (dataHasCode != hasCode) {
                    // 将物品的HasCode更新到现在的版本
                    plugin.getItemUtil().setNBT(item, "HasCode", String.valueOf(dataHasCode));
                    // 更新时耐久度百分比不变
                    List<String> dataItemLore = dataMeta.getLore();
                    for (int i = 0; i < dataItemLore.size(); i++) {
                        // 判断是否有耐久度
                        if (dataItemLore.get(i).contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                            List<String> itemLoreList = item.getItemMeta().getLore();
                            for (String itemLote : itemLoreList) {
                                //判断原来是否有耐久lore
                                if (itemLote.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                                    double maxDefaultDurability = SubCondition.getMaxDurability(itemLote);
                                    double defaultDurability = SubCondition.getDurability(itemLote);
                                    String lore = dataItemLore.get(i);
                                    double maxDurability = SubCondition.getMaxDurability(lore);
                                    // 根据当前默认耐久百分比，乘以当前RPG最大耐久条得出目前RPG耐久值
                                    lore = replaceColor(clearColor(lore).replaceFirst(String.valueOf(DurabilityCondition.getDurability(lore)), String.valueOf((int) (defaultDurability / maxDefaultDurability * maxDurability))));
                                    dataItemLore.set(i, lore);
                                    dataMeta.setLore(dataItemLore);
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    // 设置lore及名字
                    item.setItemMeta(dataMeta);
                    // 设置物品类型
                    item.setType(dataItem.getType());
                    // 设置物品子ID
                    if (dataItem.getType().getMaxDurability() == 0) {
                        item.setDurability(dataItem.getDurability());
                    }
                }
            }
        }
    }


    public void importItem(String itemName, ItemStack itemStack) throws IOException, InvalidConfigurationException {
        YamlConfiguration itemData = new YamlConfiguration();
        if (itemImportFile.exists() && !itemImportFile.isDirectory()) {
            itemData.load(itemImportFile);
        }
        itemData.set(itemName,itemStack);
        itemData.save(itemImportFile);
        itemMap.put(itemName, new ItemData(itemName, itemStack, true, Collections.singletonList(String.valueOf(itemStack.getTypeId())), null, 0));
    }

    /**
     * 保存物品
     *
     * @param itemName  String
     * @param itemStack ItemStack
     * @throws IOException                   IOException
     * @throws InvalidConfigurationException InvalidConfigurationException
     */
    @SuppressWarnings("deprecation")
    public void saveItem(String itemName, ItemStack itemStack) throws IOException, InvalidConfigurationException {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemStack.getType();
        String name = null;
        if (itemMeta.hasDisplayName()) {
            name = itemMeta.getDisplayName().replace("§", "&");
        }
        String id = String.valueOf(itemStack.getTypeId());
        if (itemStack.getType().getMaxDurability() != 0) {
            id += ":" + itemStack.getDurability();
        }
        List<String> lore = itemMeta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, lore.get(i).replace("§", "&"));
            }
        }
        YamlConfiguration itemData = new YamlConfiguration();
        if (itemDefaultFile.exists() && !itemDefaultFile.isDirectory()) {
            itemData.load(itemDefaultFile);
        }
        List<String> enchantList = new ArrayList<>();
        if (itemMeta.hasEnchants()) {
            for (Enchantment enchant : itemMeta.getEnchants().keySet()) {
                enchantList.add(enchant.getName() + ":" + itemMeta.getEnchants().get(enchant));
            }
        }
        List<String> itemFlagList = new ArrayList<>();
        if (itemMeta.getItemFlags().size() > 0) {
            for (ItemFlag itemFlag : itemMeta.getItemFlags()) {
                itemFlagList.add(itemFlag.name());
            }
        }
        String color = null;
        if (itemMeta instanceof LeatherArmorMeta) {
            Color c = ((LeatherArmorMeta) itemMeta).getColor();
            color = c.getRed() + "," + c.getGreen() + "," + c.getBlue();
        }
        String skullName = null;
        if (itemMeta instanceof SkullMeta) {
            skullName = ((SkullMeta) itemMeta).getOwner();
        }

        itemData.set(itemName + ".Name", name);
        itemData.set(itemName + ".ID", id);
        if (lore != null) itemData.set(itemName + ".Lore", lore);
        if (enchantList.size() > 0) itemData.set(itemName + ".EnchantList", enchantList);
        if (itemFlagList.size() > 0) itemData.set(itemName + ".ItemFlagList", itemFlagList);
        itemData.set(itemName + ".Unbreakable", SubCondition.getUnbreakable(itemMeta));
        itemData.set(itemName + ".Color", color);
        itemData.set(itemName + ".SkullName", skullName);
        itemData.save(itemDefaultFile);
        loadItemMap();
    }

    /**
     * 发送物品列表给指令者
     *
     * @param sender  CommandSender
     * @param searchs String
     */
    public void sendItemMapToPlayer(CommandSender sender, String... searchs) {
        if (sender instanceof Player) {
            sender.sendMessage("§e物品列表§b - §e点击获取");
        } else {
            sender.sendMessage("§e物品列表");
        }
        String search = "";
        if (searchs.length > 0) {
            search = searchs[0];
            sender.sendMessage("§c正在搜索关键词: " + search);
        }
        int z = 1;
        for (String key : itemMap.keySet()) {
            ItemData itemData = itemMap.get(key);
            ItemStack item = itemData.getItem();

            ItemMeta itemMeta = item.getItemMeta();
            String itemName = item.getType().name();
            if (itemMeta.hasDisplayName()) {
                itemName = itemMeta.getDisplayName();
            }
            //搜索功能！
            String str = "§b" + z + " - §a" + key + " §7(" + itemName + "§7)" + (itemData.isImportItem() ? " §8[§cImportItem§8]" : "");
            if (!str.contains(search)) {
                continue;
            }
            z++;
            List<String> itemLore = itemMeta.getLore();
            if (sender instanceof Player) {
                List<String> ids = itemData.getIds();
                StringBuilder id = new StringBuilder(ids.get(0));
                if (ids.size() > 1) {
                    for (int i = 1; i < ids.size(); i++) {
                        id.append("/").append(ids.get(i));
                    }
                }
                List<String> loreList = new ArrayList<>();
                loreList.add(itemName + "&b - " + id);
                if (itemLore != null) {
                    loreList.addAll(itemLore);
                } else {
                    loreList.add("&cN/A");
                }
                ((Player) sender).spigot().sendMessage(Message.getTextComponent(str, "/sxAttribute give " + key, loreList));
            } else {
                sender.sendMessage(str);
            }
        }
        if (z == 1 && searchs.length > 0) {
            sender.sendMessage("§c搜索失败! 请核对关键词!");
        }
    }
}
