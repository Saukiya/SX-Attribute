package github.saukiya.sxattribute.data.attribute;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.sub.damage.*;
import github.saukiya.sxattribute.data.attribute.sub.defence.*;
import github.saukiya.sxattribute.data.attribute.sub.other.ExpAdditionAttribute;
import github.saukiya.sxattribute.data.attribute.sub.update.SpeedAttribute;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.data.eventdata.sub.PlayerEventData;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Saukiya
 */
public class SXAttributeManager {

    static final AttributeMap attributeMap = SubAttribute.attributeMap;

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

        new BlindnessAttribute().registerAttribute();
        new CritAttribute().registerAttribute();
        new DamageAttribute().registerAttribute();
        new HitRateAttribute().registerAttribute();
        new IgnitionAttribute().registerAttribute();
        new LifeStealAttribute().registerAttribute();
        new LightningAttribute().registerAttribute();
        new PoisonAttribute().registerAttribute();
        new RealAttribute().registerAttribute();
        new SlownessAttribute().registerAttribute();
        new TearingAttribute().registerAttribute();
        new WitherAttribute().registerAttribute();

        new BlockAttribute().registerAttribute();
        new DefenseAttribute().registerAttribute();
        new DodgeAttribute().registerAttribute();
        new ReflectionAttribute().registerAttribute();
        new ToughnessAttribute().registerAttribute();

        new ExpAdditionAttribute().registerAttribute();
        new HealthRegenAttribute().registerAttribute();

        new HealthAttribute().registerAttribute();
        new SpeedAttribute().registerAttribute();
    }

    public void onAttributeEnable(){
        attributeMap.values().forEach(SubAttribute::onEnable);
    }

    /**
     * 获取一份SXAttributeList
     *
     * @return Map
     */
    static Map<Integer, SubAttribute> cloneSXAttributeList() {
        return attributeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().newAttribute(), (a, b) -> b, TreeMap::new));
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
        SXAttributeData sxAttributeData = new SXAttributeData();
        for (int i = 0; i < itemArray.length; i++) {
            ItemStack item = itemArray[i];
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                String itemName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
                if (itemName.contains("§X")) {
                    itemArray[i] = null;
                    continue;
                }

                SXAttributeData sxAttributeData1 = getListStats(entity, type, item.getItemMeta().getLore());
                if (sxAttributeData1 == null) {
                    itemArray[i] = null;
                    continue;
                }
                sxAttributeData.add(sxAttributeData1);
            }
        }
        return sxAttributeData;
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
                    if (subCondition.determine(entity, null, lore)) {
                        return null;
                    }
                }
            }
            for (SubAttribute sxAttribute : sxAttributeData.getAttributeMap().values()) {
                if (sxAttribute.loadAttribute(lore)) break;
            }
        }
        return sxAttributeData;
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
                        if (subCondition.determine(entity, item, lore)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 更新玩家血量、移动速度、血量压缩值
     *
     * @param player Player
     */
    public void updateStatsEvent(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Config.isClearDefaultAttributeAll()) {
                    plugin.getItemUtil().clearAttribute(player);
                } else if (Config.isClearDefaultAttributeReset()) {
                    plugin.getItemUtil().removeAttribute(player);
                }
                PlayerEventData playerEventData = new PlayerEventData(player);
                SXAttributeData attributeData = getEntityData(player);
                for (SubAttribute subAttribute : attributeData.getAttributeMap().values()) {
                    if (subAttribute.containsType(SXAttributeType.UPDATE)) {
                        subAttribute.eventMethod(playerEventData);
                    }
                }
            }
        }.runTask(SXAttribute.getPlugin());
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
        equipmentMap.put(uuid, attributeData);
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
        data.add(defaultAttributeData);
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
        if (!(entity instanceof Player) && attributeData.calculationValue() <= 0) {
            equipmentMap.remove(entity.getUniqueId());
        } else {
            equipmentMap.put(entity.getUniqueId(), attributeData);
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
            if (itemList.size() > 0) {
                ItemStack[] items = itemList.toArray(new ItemStack[0]);
                SXAttributeData attributeData = getItemData(player, SXConditionType.SLOT, items);
                Bukkit.getPluginManager().callEvent(new UpdateStatsEvent(StatsUpdateType.SLOT, player, attributeData, items));
                slotMap.put(player.getUniqueId(), attributeData);
            } else {
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
        if (mainItem != null && mainItem.hasItemMeta() && mainItem.getItemMeta().hasLore()) {
            for (String lore : mainItem.getItemMeta().getLore()) {
                if (mainItem == null) break;
                for (SubCondition subCondition : plugin.getConditionManager().getConditionMap().values()) {
                    if (subCondition.containsType(SXConditionType.MAIN_HAND, true)) {
                        if (subCondition.determine(entity, mainItem, lore)) {
                            mainItem = null;
                            break;
                        }
                    }
                }
            }
        }
        // 判断副手 手持
        if (offItem != null && offItem.hasItemMeta() && offItem.getItemMeta().hasLore()) {
            for (String lore : offItem.getItemMeta().getLore()) {
                if (offItem == null) break;
                for (SubCondition subCondition : plugin.getConditionManager().getConditionMap().values()) {
                    if (subCondition.containsType(SXConditionType.OFF_HAND, true)) {
                        if (subCondition.determine(entity, offItem, lore)) {
                            offItem = null;
                            break;
                        }
                    }
                }
            }
        }
        SXAttributeData attributeData = getItemData(entity, null, offItem, mainItem);
        if (attributeData.calculationValue() <= 0) {
            handMap.remove(entity.getUniqueId());
            return;
        }
        Bukkit.getPluginManager().callEvent(new UpdateStatsEvent(StatsUpdateType.HAND, entity, attributeData, offItem, mainItem));
        handMap.put(entity.getUniqueId(), attributeData);
    }
}
