package github.saukiya.sxattribute.api;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sx 临时api名
 *
 * @author Saukiya
 */
public class SXAPI {

    private static Map<UUID, Map<Class<?>, SXAttributeData>> map = new ConcurrentHashMap<>();

    public SXAttributeData getAPIAttribute(UUID uuid) {
        SXAttributeData attributeData = new SXAttributeData();
        for (SXAttributeData data : map.getOrDefault(uuid, new HashMap<>()).values()) {
            attributeData.add(data);
        }
        return attributeData;
    }

    /**
     * 为抛射物设定数据，例如箭、雪球、烈焰球。
     * 本插件只会在 EntityShootBowEvent 中附加属性
     * 如需添加其他请自行添加抛射物
     *
     * @param uuid          实体UUID
     * @param attributeData / null
     */
    public void setProjectileData(UUID uuid, SXAttributeData attributeData) {
        if (attributeData != null && attributeData.isValid()) {
            SXAttribute.getAttributeManager().getEntityDataMap().put(uuid, attributeData);
        }
    }

    /**
     * 获取抛射物数据，例如箭、雪球、烈焰球。
     *
     * @param uuid 实体UUID
     * @return SXAttributeData / null
     */
    public SXAttributeData getProjectileData(UUID uuid) {
        return SXAttribute.getAttributeManager().getEntityDataMap().get(uuid);
    }

    /**
     * 获取实体属性数据 更改无效
     *
     * @param livingEntity LivingEntity
     * @return SXAttributeData
     */
    public SXAttributeData getEntityData(LivingEntity livingEntity) {
        return SXAttribute.getAttributeManager().getEntityData(livingEntity);
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
    public boolean hasEntityAPIData(Class<?> c, UUID uuid) {
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
        map.computeIfAbsent(uuid, k -> new HashMap<>()).put(c, attributeData);
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
        Map<Class<?>, SXAttributeData> map = SXAPI.map.get(uuid);
        return map != null ? map.remove(c) : null;
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
     * 判断玩家是否达到使用物品要求
     * SXConditionType 为判断位置 一般情况为ALL
     *
     * @param entity LivingEntity
     * @param type   SXConditionType
     * @param list   ItemStack
     * @return boolean
     */
    public boolean isUse(LivingEntity entity, EquipmentType type, List<String> list) {
        return SXAttribute.getConditionManager().isUse(entity, type, list);
    }

    /**
     * 获取List的SXAttributeData数据
     *
     * @param list List
     * @return SXAttributeData
     */
    public SXAttributeData loadListData(List<String> list) {
        return SXAttribute.getAttributeManager().loadListData(list);
    }

    /**
     * 获取物品的SXAttributeData数据，可以是多个
     * (entity/type 为null 时不进行条件判断)
     * 不满足条件的ItemStack将会在数组内设置为null
     * 如果全部物品都无法识别到属性，那么返回null
     *
     * @param entity       LivingEntity
     * @param preLoadItems PreLoadItem[]
     * @return SXAttributeData
     */
    public SXAttributeData loadItemData(LivingEntity entity, PreLoadItem... preLoadItems) {
        return SXAttribute.getAttributeManager().loadItemData(entity, Arrays.asList(preLoadItems));
    }

    @Deprecated
    public SXAttributeData loadItemData(LivingEntity entity, ItemStack... items) {
        return SXAttribute.getApi().loadItemData(entity, Arrays.stream(items).map(PreLoadItem::new).toArray(PreLoadItem[]::new));
    }

    /**
     * 获取被本插件修改过的原名
     *
     * @param entity LivingEntity
     * @return String
     */
    public String getEntityName(LivingEntity entity) {
        return SXAttribute.getListenerHealthChange().getEntityName(entity);
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
     * 更新玩家装备属性
     * RPGInventory运行的情况下，不更新装备属性(特殊情况)
     *
     * @param entity LivingEntity
     */
    public void updateData(LivingEntity entity) {
        SXAttribute.getAttributeManager().loadEntityData(entity);
    }

    /**
     * UPDATE类属性更新
     *
     * @param entity LivingEntity
     */
    public void attributeUpdate(LivingEntity entity) {
        SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
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
        return SXAttribute.getItemDataManager().getItem(itemKey, player);
    }

    public double getMaxHealth(LivingEntity entity) {
        return SXAttribute.getVersionSplit()[1] > 8 ? entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() : entity.getMaxHealth();
    }

    /**
     * 返回是否存在物品
     *
     * @param itemKey String
     * @return ItemStack
     */
    public boolean hasItem(String itemKey) {
        return SXAttribute.getItemDataManager().hasItem(itemKey);
    }

    /**
     * 获取物品编号列表
     *
     * @return Set
     */
    public Set<String> getItemList() {
        return SXAttribute.getItemDataManager().getItemList();
    }
}
