package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.listener.OnItemDurabilityListener;
import github.saukiya.sxattribute.util.Config;
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
     * 快速生成物品
     *
     * @param itemName     String
     * @param id           String
     * @param itemLore     List
     * @param itemFlagList List
     * @param unbreakable  Boolean
     * @param color        String
     * @param skullName    String
     * @return ItemStack
     */
    @SuppressWarnings("deprecation")
    private static ItemStack getItemStack(String itemName, String id, List<String> itemLore, List<String> itemFlagList, Boolean unbreakable, String color, String skullName) {
        int itemMaterial = 0;
        int itemDurability = 0;
        if (id != null) {
            if (id.contains(":") && id.split(":")[0].replaceAll("[^0-9]", "").length() > 0) {
                String[] idSplit = id.split(":");
                if (idSplit[0].replaceAll("[^0-9]", "").length() > 0 && idSplit[1].replaceAll("[^0-9]", "").length() > 0) {
                    itemMaterial = Integer.valueOf(idSplit[0].replaceAll("[^0-9]", ""));
                    itemDurability = Integer.valueOf(idSplit[1].replaceAll("[^0-9]", ""));
                }
            } else if (id.replaceAll("[^0-9]", "").length() > 0) {
                itemMaterial = Integer.valueOf(id.replaceAll("[^0-9]", ""));
            }
        }
        ItemStack item = new ItemStack(itemMaterial, 1, (short) itemDurability);
        if (item.getType().name().equals(Material.AIR.name())) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cItem §4" + itemName + "§c ID Error: §4'" + id + "'§c!");
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName.replace("&", "§"));
        }
        if (itemLore != null) {
            for (int i = 0; i < itemLore.size(); i++) {
                itemLore.set(i, itemLore.get(i).replace("&", "§"));
            }
            meta.setLore(itemLore);
        }
        if (itemFlagList != null && itemFlagList.size() > 0) {
            for (String flagName : itemFlagList) {
                try {
                    ItemFlag itemFlag = ItemFlag.valueOf(flagName);
                    meta.addItemFlags(itemFlag);
                } catch (NullPointerException | IllegalArgumentException e) {
                    Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c物品: §4" + itemName + " §c的Flag: §4" + flagName + "§c 不是正常的Flag名称！");
                }
            }
        }
        if (unbreakable != null) {
            if (SXAttribute.getVersionSplit()[1] >= 11) {
                //1.11.2方法
                meta.setUnbreakable(unbreakable);
            } else {
                //1.9.0方法
                meta.spigot().setUnbreakable(unbreakable);
            }
        }
        if (color != null && meta instanceof LeatherArmorMeta) {
            Color c = Color.fromRGB(Integer.valueOf(color.split(",")[0]), Integer.valueOf(color.split(",")[1]), Integer.valueOf(color.split(",")[2]));
            ((LeatherArmorMeta) meta).setColor(c);
        }
        if (skullName != null && meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(skullName);
        }
        item.setItemMeta(meta);
        return item;
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
        for (File file : Objects.requireNonNull(files.listFiles()))
            if (file.isDirectory()) {
                loadItem(file);
            } else {
                YamlConfiguration itemYml = new YamlConfiguration();
                itemYml.load(file);
                for (String name : itemYml.getKeys(false)) {
                    if (itemMap.containsKey(name)) {
                        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cDon't Repeat Item Name: §4" + file.getName() + File.separator + name + " §c!");
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

                    ItemStack item = getItemStack(itemName, id, itemLore, itemFlagList, unbreakable, colorStr, skullName);
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

                    itemMap.put(name, new ItemData(item, ids, enchantList));
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
     * @param players  Player
     * @return ItemStack
     */
    @SuppressWarnings("deprecation")
    public ItemStack getItem(String itemName, Player... players) {
        if (itemMap.containsKey(itemName)) {
            ItemData itemData = itemMap.get(itemName);
            ItemStack item = itemData.getItem().clone();
            Map<String, String> lockRandomMap = new HashMap<>();
            String id = plugin.getRandomStringManager().processRandomString(itemName, itemData.getIds().get(SXAttribute.getRandom().nextInt(itemData.getIds().size())), lockRandomMap);
            int itemMaterial = 260, itemDurability = 0;
            if (id != null) {
                if (id.contains(":")) {
                    String[] idSplit = id.split(":");
                    itemMaterial = Integer.valueOf(idSplit[0]);
                    itemDurability = Integer.valueOf(idSplit[1]);
                } else {
                    itemMaterial = Integer.valueOf(id);
                }
            }
            item.setTypeId(itemMaterial);
            item.setDurability((short) itemDurability);
            if (item.getType().name().equals(Material.AIR.name())) {
                Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cItem §4" + itemName + "§c Error ID! Please Check ID: §4" + id + "§c!");
                return null;
            }
            ItemMeta meta = item.getItemMeta();
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                if (meta.hasDisplayName()) {
                    String name = plugin.getRandomStringManager().processRandomString(itemName, meta.getDisplayName(), lockRandomMap);
                    meta.setDisplayName(name.replace("&", "§").replace("%DeleteLore%", ""));
                }
                List<String> loreList = meta.getLore();
                for (int i = loreList.size() - 1; i >= 0; i--) {
                    String lore = plugin.getRandomStringManager().processRandomString(itemName, loreList.get(i), lockRandomMap);
                    // 计算耐久值
                    if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                        // 识别物品是否为工具
                        if (item.getType().getMaxDurability() > 0) {
                            if (!OnItemDurabilityListener.getUnbreakable(meta)) {
                                Repairable repairable = (Repairable) meta;
                                repairable.setRepairCost(999);
                                meta = (ItemMeta) repairable;
                                int durability = OnItemDurabilityListener.getDurability(lore);
                                int maxDurability = OnItemDurabilityListener.getMaxDurability(lore);
                                int maxDefaultDurability = item.getType().getMaxDurability();
                                int defaultDurability = (int) (((double) durability / maxDurability) * maxDefaultDurability);
                                item.setDurability((short) (maxDefaultDurability - defaultDurability));
                            }
                        }
                    }
                    if (lore.contains("%DeleteLore%")) {
                        loreList.remove(i);
                    } else {
                        lore = lore.replace("&", "§");
                        if (lore.contains("\n") || lore.contains("/n")) {
                            loreList.remove(i);
                            loreList.addAll(i, Arrays.asList(lore.replace("/n","\n").split("\n")));
                        } else {
                            loreList.set(i, lore);
                        }
                    }
                }
                if (SXAttribute.isPlaceholder() && players.length > 0 && players[0] != null) {
                    loreList = PlaceholderAPI.setPlaceholders(players[0], loreList);
                }
                meta.setLore(loreList);
                item.setItemMeta(meta);
            }
            if (itemData.getEnchantList() != null && itemData.getEnchantList().size() > 0) {
                List<String> enchantList = new ArrayList<>();
                for (String enchantName : itemData.getEnchantList()) {
                    enchantName = plugin.getRandomStringManager().processRandomString(itemName, enchantName, lockRandomMap);
                    if (enchantName.contains("\n")  || enchantName.contains("/n")) {
                        enchantList.addAll(Arrays.asList(enchantName.replace("/n","\n").split("\n")));
                    } else {
                        enchantList.add(enchantName);
                    }
                }
                for (String enchantName : enchantList) {
                    if (enchantName.contains(":") && enchantName.split(":").length > 1) {
                        Enchantment enchant = Enchantment.getByName(enchantName.split(":")[0]);
                        int level = Integer.valueOf(enchantName.split(":")[1].replaceAll("[^0-9]", ""));
                        if (enchant != null) {
                            if (level > 0) {
                                meta.addEnchant(enchant, level, true);
                            }
                        } else {
                            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c物品: §4" + itemName + " §c的附魔: §4" + enchantName.split(":")[0] + "§c 不是正常的附魔名称！");
                        }
                    }
                }
            }
            item.setItemMeta(meta);
            // 存储lockRandomMap
            if (lockRandomMap.size() > 0) {
                List<String> list = new ArrayList<>();
                lockRandomMap.forEach((key, value) -> list.add(key + "§e§l§k|§e§r" + value));
                plugin.getItemUtil().setNBTList(item, "LockRandomMap", list);
            }
            if (Config.isDamageGauges()) {
                return plugin.getItemUtil().setAttackSpeed(item);
            } else {
                return item;
            }
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
            if (isItem(dataName)) {
                // 获取物品的HashCode
                int hasCode = Integer.valueOf(Objects.requireNonNull(plugin.getItemUtil().getNBT(item, "HasCode")));
                ItemStack dataItem = getItem(dataName, player);
                assert dataItem != null;
                ItemMeta dataMeta = dataItem.getItemMeta();
                // 获取ItemMap物品的HashCode
                int dataHasCode = Integer.valueOf(Objects.requireNonNull(plugin.getItemUtil().getNBT(dataItem, "HasCode")));
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
                                    double maxDefaultDurability = OnItemDurabilityListener.getMaxDurability(itemLote);
                                    double defaultDurability = OnItemDurabilityListener.getDurability(itemLote);
                                    String lore = dataItemLore.get(i);
                                    double maxDurability = OnItemDurabilityListener.getMaxDurability(lore);
                                    // 根据当前默认耐久百分比，乘以当前RPG最大耐久条得出目前RPG耐久值
                                    lore = replaceColor(clearColor(lore).replaceFirst(String.valueOf(OnItemDurabilityListener.getDurability(lore)), String.valueOf((int) (defaultDurability / maxDefaultDurability * maxDurability))));
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
        itemData.set(itemName + ".Unbreakable", OnItemDurabilityListener.getUnbreakable(itemMeta));
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
            String str = "§b" + z + " - §a" + key + " §7(" + itemName + "§7)";
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
                Message.sendCommandToPlayer((Player) sender, str, "/sxAttribute give " + key, loreList);
            } else {
                sender.sendMessage(str);
            }
        }
        if (z == 1 && searchs.length > 0) {
            sender.sendMessage("§c搜索失败! 请核对关键词!");
        }
    }
}
