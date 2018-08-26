package github.saukiya.sxattribute.data.attribute;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.sub.damage.*;
import github.saukiya.sxattribute.data.attribute.sub.defence.*;
import github.saukiya.sxattribute.data.attribute.sub.other.ExpAdditionAttribute;
import github.saukiya.sxattribute.data.attribute.sub.update.SpeedAttribute;
import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateEventData;
import github.saukiya.sxattribute.event.StatsUpdateType;
import github.saukiya.sxattribute.event.UpdateStatsEvent;
import github.saukiya.sxattribute.util.Config;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.InventoryManager;

import java.util.*;

/**
 * 属性管理器
 *
 * @author Saukiya
 */
public class SXAttributeManager {

    private static final AttributeMap attributeMap = SubAttribute.attributeMap;

    @Getter
    private final Map<UUID, SXAttributeData> rpgInventoryMap = new HashMap<>();
    @Getter
    private final Map<UUID, SXAttributeData> equipmentMap = new HashMap<>();
    @Getter
    private final Map<UUID, SXAttributeData> handMap = new HashMap<>();
    @Getter
    private final Map<UUID, SXAttributeData> slotMap = new HashMap<>();

    private final SXAttribute plugin;

    private SXAttributeData defaultAttributeData;

    public SXAttributeManager(SXAttribute plugin) {
        this.plugin = plugin;

        new BlindnessAttribute().registerAttribute(plugin);
        new CritAttribute().registerAttribute(plugin);
        new DamageAttribute().registerAttribute(plugin);
        new HitRateAttribute().registerAttribute(plugin);
        new IgnitionAttribute().registerAttribute(plugin);
        new LifeStealAttribute().registerAttribute(plugin);
        new LightningAttribute().registerAttribute(plugin);
        new PoisonAttribute().registerAttribute(plugin);
        new RealAttribute().registerAttribute(plugin);
        new SlownessAttribute().registerAttribute(plugin);
        new TearingAttribute().registerAttribute(plugin);
        new WitherAttribute().registerAttribute(plugin);

        new BlockAttribute().registerAttribute(plugin);
        new DefenseAttribute().registerAttribute(plugin);
        new DodgeAttribute().registerAttribute(plugin);
        new ReflectionAttribute().registerAttribute(plugin);
        new ToughnessAttribute().registerAttribute(plugin);

        new ExpAdditionAttribute().registerAttribute(plugin);
        new HealthRegenAttribute().registerAttribute(plugin);

        new HealthAttribute().registerAttribute(plugin);
        new SpeedAttribute().registerAttribute(plugin);
    }

    /**
     * 获取一份SXAttributeList
     *
     * @return Map
     */
    static Map<Integer, SubAttribute> cloneSXAttributeList() {
        TreeMap<Integer, SubAttribute> map = new TreeMap<>();
        for (Map.Entry<Integer, SubAttribute> entry : attributeMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().newAttribute());
        }
        return map;
    }

    public void onAttributeEnable() {
        attributeMap.values().forEach(SubAttribute::onEnable);
    }


    public void onAttributeDisable() {
        attributeMap.values().forEach(SubAttribute::onDisable);
    }

    /**
     * 获取物品的属性
     *
     * @param entity    LivingEntity 可以为Null 则不进行条件判断
     * @param type      SXConditionType 可以为Null 则不进行条件判断
     * @param itemArray ItemStack[] 物品列表 不满足条件的物品修改为null
     * @return SXAttributeData
     */
    public SXAttributeData getItemData(LivingEntity entity, SXConditionType type, ItemStack... itemArray) {
        SXAttributeData sxAttributeDataList = new SXAttributeData();
        for (int i = 0; i < itemArray.length; i++) {
            ItemStack item = itemArray[i];
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                String itemName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
                if (itemName.contains("§X")) {
                    itemArray[i] = null;
                    continue;
                }
                SXAttributeData sxAttributeData = new SXAttributeData();
                for (String lore : item.getItemMeta().getLore()) {
                    if (!lore.contains("§X")) {
                        for (SubCondition subCondition : plugin.getConditionManager().getConditionMap().values()) {
                            if (subCondition.containsType(type, true)) {
                                SXConditionReturnType returnType = subCondition.determine(entity, item, lore);
                                if (returnType.equals(SXConditionReturnType.ITEM)) {
                                    sxAttributeData = null;
                                    break;
                                }else if (returnType.equals(SXConditionReturnType.LORE)){
                                    break;
                                }
                            }
                        }
                        if (sxAttributeData == null) {
                            itemArray[i] = null;
                            break;
                        }
                        for (SubAttribute sxAttribute : sxAttributeData.getAttributeMap().values()) {
                            if (sxAttribute.loadAttribute(lore)){
                                sxAttributeData.valid();
                                break;
                            }
                        }
                    }
                }
                sxAttributeDataList.add(sxAttributeData);
            }
        }
        return sxAttributeDataList.isValid() ? sxAttributeDataList : null;
    }

    /**
     * 获取Lore的属性
     * 带有§X 的一行不被识别属性
     *
     * @param entity     如果有玩家 那么判断玩家是否满足条件才可使用该物品
     * @param type       更新位置类型 可以为Null 则不进行条件判断
     * @param stringList 物品lore，也可以是其他存有属性的list
     * @return 满足条件返回 SXAttributeData 不满足返回null
     */
    public SXAttributeData getListStats(LivingEntity entity, SXConditionType type, List<String> stringList) {
        SXAttributeData sxAttributeData = new SXAttributeData();
        for (String lore : stringList) {
            if (lore.contains("§X")) {
                continue;
            }
            for (SubCondition subCondition : plugin.getConditionManager().getConditionMap().values()) {
                if (subCondition.containsType(type, true)) {
                    SXConditionReturnType returnType = subCondition.determine(entity, null, lore);
                    if (returnType.equals(SXConditionReturnType.ITEM)) {
                        return null;
                    }else if (returnType.equals(SXConditionReturnType.LORE)){
                        break;
                    }
                }
            }
            for (SubAttribute sxAttribute : sxAttributeData.getAttributeMap().values()) {
                if (sxAttribute.loadAttribute(lore)){
                    sxAttributeData.valid();
                    break;
                }
            }
        }
        return sxAttributeData.isValid() ? sxAttributeData : null;
    }

    /**
     * 判断物品是否符合使用条件
     *
     * @param entity 实体
     * @param item   物品
     * @param type   物品所处位置
     * @return boolean
     */
    public Boolean isUse(LivingEntity entity, SXConditionType type, ItemStack item) {
        if (type != null && item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> loreList = item.getItemMeta().getLore();
            for (String lore : loreList) {
                for (SubCondition subCondition : plugin.getConditionManager().getConditionMap().values()) {
                    if (subCondition.containsType(type, true)) {
                        if (subCondition.determine(entity, item, lore).equals(SXConditionReturnType.ITEM)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 更新实体UPDATE类属性
     *
     * @param entity Player
     */
    public void updateStatsEvent(LivingEntity entity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (entity instanceof Player) {
                    if (Config.isClearDefaultAttributeAll()) {
                        plugin.getItemUtil().clearAttribute((Player) entity);
                    } else if (Config.isClearDefaultAttributeReset()) {
                        plugin.getItemUtil().removeAttribute((Player) entity);
                    }
                }
                UpdateEventData updateEventData = new UpdateEventData(entity);
                SXAttributeData attributeData = getEntityData(entity);
                for (SubAttribute subAttribute : attributeData.getAttributeMap().values()) {
                    if (subAttribute.containsType(SXAttributeType.UPDATE)) {
                        subAttribute.eventMethod(updateEventData);
                    }
                }
            }
        }.runTask(plugin);
    }

    public void loadDefaultAttributeData() {
        defaultAttributeData = getListStats(null, null, Config.getConfig().getStringList(Config.DEFAULT_STATS));
    }

    // 读取抛射物数据
    public SXAttributeData getProjectileData(UUID uuid) {
        return new SXAttributeData().add(equipmentMap.remove(uuid));
    }

    //设置抛射物的数据
    public void setProjectileData(UUID uuid, SXAttributeData attributeData) {
        if (attributeData != null && attributeData.isValid()){
            equipmentMap.put(uuid, attributeData);
        } else {
            equipmentMap.remove(uuid);
        }
    }

    // 获取生物总数据
    public SXAttributeData getEntityData(LivingEntity entity, SXAttributeData... attributeData) {
        SXAttributeData data = new SXAttributeData();
        UUID uuid = entity.getUniqueId();
        if (SXAttribute.isRpgInventory() && entity instanceof Player) {
            data.add(rpgInventoryMap.get(uuid));
        } else {
            data.add(equipmentMap.get(uuid));
            data.add(slotMap.get(uuid));
        }
        if (attributeData.length > 0) {
            data.add(attributeData[0]);
        } else {
            data.add(handMap.get(uuid));
        }
        //TODO API获取
        data.add(SXAttribute.getApi().getAPIStats(entity.getUniqueId()));
        // 计算点数
        data.calculationValue();
        // 生物默认数据
        if (entity instanceof Player) {
            data.add(defaultAttributeData);
        }
        // 纠正数值
        data.correct();
        return data;
    }

    /**
     * 清除生物数据
     *
     * @param uuid EntityUUID
     */
    public void clearEntityData(UUID uuid) {
        equipmentMap.remove(uuid);
        handMap.remove(uuid);
        slotMap.remove(uuid);
        rpgInventoryMap.remove(uuid);
    }

    // 加载生物装备槽的数据
    public void loadRPGInventoryData(Player player) {
        if (SXAttribute.isRpgInventory()) {
            Inventory inv = InventoryManager.getInventory(player);
            if (inv != null) {
                List<ItemStack> itemList = new ArrayList<>();
                List<Integer> whiteSlotList = Config.getConfig().getIntegerList(Config.PRG_INVENTORY__WHITE_SLOT);
                for (int i = 0; i < 54; i++) {
                    if (!whiteSlotList.contains(i)) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && item.getItemMeta().hasLore()) itemList.add(item);
                    }
                }
                SXAttributeData data = getItemData(player, SXConditionType.RPG_INVENTORY, itemList.toArray(new ItemStack[0]));
                Bukkit.getPluginManager().callEvent(new UpdateStatsEvent(StatsUpdateType.RPG_INVENTORY, player, data, itemList.toArray(new ItemStack[0])));
                if (data != null){
                    rpgInventoryMap.put(player.getUniqueId(), data);
                } else {
                    rpgInventoryMap.remove(player.getUniqueId());
                }
            }
        }
    }

    // 加载生物装备槽的数据
    public void loadEquipmentData(LivingEntity entity) {
        if (SXAttribute.isRpgInventory() && entity instanceof Player) {
            return;
        }
        // 更新物品
        if (Config.isItemUpdate() && entity instanceof Player) {
            for (ItemStack item : entity.getEquipment().getArmorContents()) {
                plugin.getItemDataManager().updateItem(item, (Player) entity);
            }
        }
        // 装备更新
        ItemStack[] itemList = entity.getEquipment().getArmorContents();
        SXAttributeData attributeData = getItemData(entity, SXConditionType.EQUIPMENT, itemList);
        Bukkit.getPluginManager().callEvent(new UpdateStatsEvent(StatsUpdateType.EQUIPMENT, entity, attributeData, itemList));
        if (attributeData != null) {
            equipmentMap.put(entity.getUniqueId(), attributeData);
        } else {
            equipmentMap.remove(entity.getUniqueId());
        }
    }

    /**
     * 加载生物装备槽的数据
     *
     * @param player Player
     */
    public void loadSlotData(Player player) {
        // 饰品更新
        if (!SXAttribute.isRpgInventory() && Config.getConfig().getStringList(Config.REGISTER_SLOTS_LIST).size() > 0) {
            List<ItemStack> itemList = new ArrayList<>();
            Inventory inv = player.getInventory();
            plugin.getRegisterSlotManager().getRegisterSlotMap().forEach((slot, registerSlot) ->
            {
                ItemStack item = inv.getItem(slot);
                if (item != null && !item.getType().equals(Material.AIR) && item.getItemMeta().hasLore() && item.getItemMeta().getLore().stream().anyMatch(lore -> lore.contains(registerSlot.getName()))) {
                    itemList.add(item);
                }
            });
            ItemStack[] items = itemList.toArray(new ItemStack[0]);
            SXAttributeData attributeData = getItemData(player, SXConditionType.SLOT, items);
            Bukkit.getPluginManager().callEvent(new UpdateStatsEvent(StatsUpdateType.SLOT, player, attributeData, items));
            if (attributeData != null){
                slotMap.put(player.getUniqueId(), attributeData);
            }else {
                slotMap.remove(player.getUniqueId());
            }
        }
    }

    // 加载生物手中的数据
    public void loadHandData(LivingEntity entity) {
        // 更新物品
        if (Config.isItemUpdate() && entity instanceof Player) {
            plugin.getItemDataManager().updateItem(entity.getEquipment().getItemInMainHand(), (Player) entity);
            plugin.getItemDataManager().updateItem(entity.getEquipment().getItemInOffHand(), (Player) entity);
        }
        ItemStack mainItem = entity.getEquipment().getItemInMainHand();
        ItemStack offItem = entity.getEquipment().getItemInOffHand();
        ItemStack[] itemArray= {mainItem,null};
        SXAttributeData attributeData = getItemData(entity, SXConditionType.MAIN_HAND, itemArray);
        itemArray[0] = null;
        itemArray[1] = offItem;
        attributeData = attributeData != null ? attributeData.add(getItemData(entity, SXConditionType.OFF_HAND, itemArray)) : getItemData(entity, SXConditionType.OFF_HAND, itemArray);
        itemArray[1] = mainItem;
        Bukkit.getPluginManager().callEvent(new UpdateStatsEvent(StatsUpdateType.HAND, entity, attributeData, itemArray));
        if (attributeData != null){
            handMap.put(entity.getUniqueId(), attributeData);
        } else {
            handMap.remove(entity.getUniqueId());
        }
    }
}
