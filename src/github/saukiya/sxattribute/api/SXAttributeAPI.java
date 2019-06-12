package github.saukiya.sxattribute.api;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.RandomStringManager;
import github.saukiya.sxattribute.data.RegisterSlot;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

/**
 * API 获取方式为 SXAttribute.getApi()
 *
 * @author Saukiya
 */
public class SXAttributeAPI {

    private final Map<UUID, Map<Class<?>, SXAttributeData>> map = new HashMap<>();
    private final SXAttribute plugin;

    /**
     * 加载SXAttributeAPI
     *
     * @param plugin SXAttribute
     */
    public SXAttributeAPI(SXAttribute plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取实体总API属性数据
     *
     * @param uuid 实体UUID
     * @return 来自所有附属插件提供的 SXAttributeData
     */
    public SXAttributeData getAPIStats(UUID uuid) {
        SXAttributeData attributeData = new SXAttributeData();
        if (map.containsKey(uuid)) {
            for (Class<?> c : map.get(uuid).keySet()) {
                attributeData.add(map.get(uuid).get(c));
            }
        }
        return attributeData;
    }

    /**
     * 获取 ItemUtil(NBT反射类)
     * key值结构为:SX-Attribute-{key}
     *
     * @return ItemUtil
     */
    public ItemUtil getItemUtil() {
        return plugin.getItemUtil();
    }

    /**
     * 获取 RandomStringManager(随机字符管理)
     *
     * @return RandomStringManager
     */
    public RandomStringManager getRandomStringManager() {
        return plugin.getRandomStringManager();
    }

    /**
     * 获取 RegisterSlotManager 中
     * map 的 entrySet
     *
     * @return RandomStringManager
     */
    public Set<Map.Entry<Integer, RegisterSlot>> getRegisterSlotMapEntrySet() {
        return plugin.getRegisterSlotManager().getRegisterSlotMap().entrySet();
    }

    /**
     * 为抛射物设定数据，例如箭、雪球、烈焰球。
     * 本插件只会在玩家射箭的时候附加属性
     * 如需添加其他请自行添加抛射物
     *
     * @param uuid          实体UUID
     * @param attributeData / null
     */
    @Nullable
    public void setProjectileData(UUID uuid, SXAttributeData attributeData) {
        plugin.getAttributeManager().setProjectileData(uuid, attributeData);
    }

    /**
     * 获取抛射物数据，例如箭、雪球、烈焰球。
     *
     * @param uuid 实体UUID
     * @return SXAttributeData
     */
    public SXAttributeData getProjectileData(UUID uuid) {
        return plugin.getAttributeManager().getProjectileData(uuid);
    }

    /**
     * 获取实体属性数据 更改无效
     * 如果添加SXAttributeData 那么sxAttributeData后则替代手持属性(并不覆盖原手持数据)
     *
     * @param livingEntity    LivingEntity
     * @param sxAttributeData SXAttributeData[]
     * @return SXAttributeData
     */
    public SXAttributeData getEntityAllData(LivingEntity livingEntity, SXAttributeData... sxAttributeData) {
        return plugin.getAttributeManager().getEntityData(livingEntity, sxAttributeData);
    }

    /**
     * 获取实体与插件关联的属性数据
     *
     * @param c    Class
     * @param uuid UUID
     * @return SXAttributeData / null
     */
    public SXAttributeData getEntityAPIData(Class<?> c, UUID uuid) {
        return map.containsKey(uuid) ? map.get(uuid).get(c) : null;
    }

    /**
     * 判断插件是否有注册该实体的属性
     *
     * @param c    Class
     * @param uuid UUID
     * @return boolean
     */
    public boolean isEntityAPIData(Class<?> c, UUID uuid) {
        return map.containsKey(uuid) && map.get(uuid).containsKey(c);
    }

    /**
     * 设置插件关联的实体属性数据
     *
     * @param c             Class
     * @param uuid          UUID
     * @param attributeData SXAttributeData
     */
    public void setEntityAPIData(Class<?> c, UUID uuid, SXAttributeData attributeData) {
        Map<Class<?>, SXAttributeData> statsMap = new HashMap<>();
        if (map.containsKey(uuid)) {
            statsMap = map.get(uuid);
        } else {
            map.put(uuid, statsMap);
        }
        statsMap.put(c, attributeData);
    }

    /**
     * 清除插件关联的实体属性数据
     * 会返回清除前的数据
     *
     * @param c    插件Class
     * @param uuid 实体UUID
     * @return SXAttributeData / null
     */
    public SXAttributeData removeEntityAPIData(Class<?> c, UUID uuid) {
        SXAttributeData attributeData = null;
        if (map.containsKey(uuid) && map.get(uuid).containsKey(c)) {
            attributeData = map.get(uuid).remove(c);
        }
        return attributeData;
    }

    /**
     * 清除插件关联的所有实体属性数据
     *
     * @param c Class
     */
    public void removePluginAllEntityData(Class<?> c) {
        for (Map<Class<?>, SXAttributeData> statsMap : map.values()) {
            statsMap.remove(c);
        }
    }

    /**
     * 清除插件所有关联的实体属性数据
     *
     * @param uuid 实体UUID
     */
    public void removeEntityAllPluginData(UUID uuid) {
        map.remove(uuid);
    }


    /**
     * 获取List的SXAttributeData数据
     * (entity/type 为null 时不进行条件判断)
     * 如果不满足条件则返回null
     *
     * @param entity   LivingEntity
     * @param type     SXConditionType
     * @param loreList List
     * @return SXAttributeData
     */
    public SXAttributeData getLoreData(LivingEntity entity, SXConditionType type, List<String> loreList) {
        return plugin.getAttributeManager().getListStats(entity, type, loreList);
    }

    /**
     * 获取物品的SXAttributeData数据，可以是多个
     * (entity/type 为null 时不进行条件判断)
     * 不满足条件的ItemStack将会在数组内设置为null
     * 如果全部物品都无法识别到属性，那么返回null
     *
     * @param livingEntity LivingEntity
     * @param type         SXConditionType
     * @param item         ItemStack[]
     * @return SXAttributeData
     */
    public SXAttributeData getItemData(LivingEntity livingEntity, SXConditionType type, ItemStack... item) {
        return plugin.getAttributeManager().getItemData(livingEntity, type, item);
    }

    /**
     * 获取被本插件修改过的原名
     *
     * @param livingEntity LivingEntity
     * @return String
     */
    public String getEntityName(LivingEntity livingEntity) {
        return plugin.getOnHealthChangeDisplayListener().getEntityName(livingEntity);
    }

    /**
     * 判断玩家是否达到使用物品要求
     * SXConditionType 为判断位置 一般情况为ALL
     *
     * @param entity LivingEntity
     * @param type   SXConditionType
     * @param item   ItemStack
     * @return boolean
     */
    public boolean isUse(LivingEntity entity, SXConditionType type, ItemStack item) {
        return plugin.getAttributeManager().isUse(entity, type, item);
    }

    /**
     * 获取物品的限制等级
     *
     * @param item ItemStack
     * @return int / -1
     */
    public int getItemLevel(ItemStack item) {
        return SubCondition.getItemLevel(item);
    }

    /**
     * 获取实体等级(如果SX-Level工作时 那将会获取SL等级)
     * 怪物目前为10000
     *
     * @param entity LivingEntity
     * @return level
     */
    public int getEntityLevel(LivingEntity entity) {
        return SubCondition.getLevel(entity);
    }

    /**
     * 更新玩家装备属性
     * RPGInventory运行的情况下，不更新装备属性(特殊情况)
     *
     * @param entity LivingEntity
     */
    public void updateEquipmentData(LivingEntity entity) {
        plugin.getAttributeManager().loadEquipmentData(entity);
    }

    /**
     * 更新玩家装备属性
     * RPGInventory运行的情况下，不更新装备属性(特殊情况)
     *
     * @param player Player
     */
    public void updateRPGInventoryData(Player player) {
        plugin.getAttributeManager().loadRPGInventoryData(player);
    }

    /**
     * 更新玩家手持属性
     *
     * @param entity LivingEntity
     */
    public void updateHandData(LivingEntity entity) {
        plugin.getAttributeManager().loadHandData(entity);
    }

    /**
     * 更新玩家自定义槽属性
     *
     * @param player Player
     */
    public void updateSlotData(Player player) {
        plugin.getAttributeManager().loadSlotData(player);
    }

    /**
     * UPDATE类属性更新
     *
     * @param entity LivingEntity
     */
    public void updateStats(LivingEntity entity) {
        plugin.getAttributeManager().updateStatsEvent(entity);
    }

    /**
     * 获取物品
     * 代入Player 则支持Placeholder变量
     *
     * @param itemKey String
     * @param player  Player
     * @return ItemStack
     */
    public ItemStack getItem(String itemKey, Player player) {
        return plugin.getItemDataManager().getItem(itemKey, player);
    }

    /**
     * 获取物品编号列表
     *
     * @return Set
     */
    public Set<String> getItemList() {
        return plugin.getItemDataManager().getItemList();
    }

    /**
     * 获取全息列表 全息插件无运行时返回null
     *
     * @return Hologram / null
     */
    public List<Hologram> getHologramsList() {
        return plugin.getOnDamageListener().getHologramsList();
    }
}
