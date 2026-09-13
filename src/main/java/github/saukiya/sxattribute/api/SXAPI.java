package github.saukiya.sxattribute.api;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.data.attribute.AttributeSource;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SXAttributeManager;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.AttributeUtil;
import github.saukiya.sxattribute.feature.source.SourceApplyRequest;
import github.saukiya.sxattribute.feature.source.SourceWriteResult;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sx 临时api名
 *
 * @author Saukiya
 */
public class SXAPI {
    /** 返回 SkillAPI 双向桥是否已完成握手并注册属性/伤害处理器。 */
    public boolean isSkillApiBridgeEnabled() {
        return github.saukiya.sxattribute.hook.skillapi.SkillApiHook.isActive();
    }

    /** Class 分源保留名前缀 (旧 API 映射为命名源 class:&lt;全类名&gt;) */
    private static final String CLASS_PREFIX = "class:";
    /** 抛射物快照保留源名 */
    private static final String PROJECTILE = "抛射物";

    private static SXAttributeManager mgr() {
        return SXAttribute.getAttributeManager();
    }

    /**
     * 汇总实体全部 Class 分源(旧 API 注入)的属性
     */
    public SXAttributeData getAPIAttribute(UUID uuid) {
        SXAttributeData attributeData = new SXAttributeData();
        for (String name : mgr().getSourceNames(uuid)) {
            if (name.startsWith(CLASS_PREFIX)) {
                AttributeSource source = mgr().getSource(uuid, name);
                if (source != null) {
                    attributeData.add(source.getData());
                }
            }
        }
        return attributeData;
    }

    // ==================== 多属性源 API (命名源) ====================

    /**
     * 添加/覆盖一个命名属性源(解析 lore), 同名替换不叠加; 触发可取消的 SXAttributeSourceAddEvent。
     *
     * @param entity 实体
     * @param source 源标识名 (如 "力量加成")
     * @param lore   属性词条行 (如 ["攻击伤害: 50"])
     * @param update 是否立即刷新 UPDATE 类属性
     * @return 是否成功 (被事件取消则 false)
     */
    public boolean addSourceAttribute(LivingEntity entity, String source, List<String> lore, boolean update) {
        return addSourceAttribute(entity, source, loadListData(lore), update);
    }

    /**
     * 添加/覆盖一个命名属性源(直接数据), 同名替换不叠加; 触发可取消的 SXAttributeSourceAddEvent。
     */
    public boolean addSourceAttribute(LivingEntity entity, String source, SXAttributeData data, boolean update) {
        boolean ok = mgr().addSource(entity, source, data, false, true);
        if (ok && update) {
            mgr().attributeUpdateEvent(entity);
        }
        return ok;
    }

    /**
     * 创建静态源(不触发事件的纯数值注入, 用于内部计算后批量注入)。
     */
    public void createStaticAttributeSource(LivingEntity entity, String source, List<String> lore, boolean update) {
        createStaticAttributeSource(entity, source, loadListData(lore), update);
    }

    public void createStaticAttributeSource(LivingEntity entity, String source, SXAttributeData data, boolean update) {
        mgr().addSource(entity, source, data, true, false);
        if (update) {
            mgr().attributeUpdateEvent(entity);
        }
    }

    /**
     * 移除一个命名源(非静态源触发 SXAttributeSourceRemoveEvent)。
     *
     * @return 被移除源的数据 (无则 null)
     */
    public SXAttributeData takeSourceAttribute(LivingEntity entity, String source, boolean update) {
        SXAttributeData removed = mgr().takeSource(entity, source, true);
        if (removed != null && update) {
            mgr().attributeUpdateEvent(entity);
        }
        return removed;
    }

    public SXAttributeData takeSourceAttribute(LivingEntity entity, String source) {
        return takeSourceAttribute(entity, source, true);
    }

    /**
     * 获取某命名源的数据 (无则 null)
     */
    public SXAttributeData getSourceAttribute(UUID uuid, String source) {
        AttributeSource s = mgr().getSource(uuid, source);
        return s != null ? s.getData() : null;
    }

    public boolean hasSourceAttribute(UUID uuid, String source) {
        return mgr().hasSource(uuid, source);
    }

    /**
     * 获取实体全部命名源的名称集合
     */
    public Set<String> getSourceNames(UUID uuid) {
        return mgr().getSourceNames(uuid);
    }

    /**
     * 读取配置化属性的命名字段。
     *
     * @param entity      实体
     * @param attributeId Feature/Attribute 定义 ID
     * @param field       Values 下的字段名
     * @return 当前聚合值，未定义返回 0
     */
    public double getDynamicAttribute(LivingEntity entity, String attributeId, String field) {
        return getEntityData(entity).getDynamicValue(attributeId, field);
    }

    /**
     * 通过统一生命周期服务施加临时或持久化来源。
     */
    public SourceWriteResult applyManagedSource(LivingEntity entity, SourceApplyRequest request) {
        return SXAttribute.getSourceService().apply(entity, request);
    }

    /**
     * 移除统一生命周期服务管理的来源。
     */
    public SourceWriteResult removeManagedSource(LivingEntity entity, String source) {
        return SXAttribute.getSourceService().remove(entity, source);
    }

    public SourceWriteResult applyManagedSourceRule(LivingEntity entity, String ruleId) {
        return SXAttribute.getSourceService().applyRule(entity, ruleId);
    }

    /**
     * 不经过 GUI 成本扣除，直接执行一个装备模块操作，供任务、NPC 或外部菜单调用。
     */
    public FeatureResult operateEquipmentFeature(Player player, ItemStack item, String featureId) {
        return SXAttribute.getForgeFeatureManager().operate(player, item, featureId);
    }

    /** 触发配置化属性的 API 事件类型。 */
    public void triggerDynamicAttributes(LivingEntity actor, LivingEntity target) {
        SXAttribute.getAttributeEngine().fireApi(actor, target);
    }

    public YamlConfiguration getEquipmentFeatureState(ItemStack item, String featureId) {
        return SXAttribute.getForgeFeatureManager().state(item, featureId);
    }

    public void setEquipmentFeatureState(Player player, ItemStack item, String featureId, YamlConfiguration state) {
        SXAttribute.getForgeFeatureManager().state(player, item, featureId, state);
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
            mgr().putSource(uuid, new AttributeSource(PROJECTILE, attributeData, true));
        }
    }

    /**
     * 获取抛射物数据，例如箭、雪球、烈焰球。
     *
     * @param uuid 实体UUID
     * @return SXAttributeData / null
     */
    public SXAttributeData getProjectileData(UUID uuid) {
        AttributeSource source = mgr().getSource(uuid, PROJECTILE);
        return source != null ? source.getData() : null;
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
        AttributeSource source = mgr().getSource(uuid, CLASS_PREFIX + c.getName());
        return source != null ? source.getData() : null;
    }

    /**
     * 判断插件是否有注册该实体的属性
     *
     * @param c    Class
     * @param uuid UUID
     * @return boolean
     */
    public boolean hasEntityAPIData(Class<?> c, UUID uuid) {
        return mgr().hasSource(uuid, CLASS_PREFIX + c.getName());
    }

    /**
     * 设置插件关联的实体属性数据 (映射为命名源 class:&lt;全类名&gt;)
     *
     * @param c             Class
     * @param uuid          UUID
     * @param attributeData SXAttributeData
     */
    public void setEntityAPIData(Class<?> c, UUID uuid, SXAttributeData attributeData) {
        mgr().putSource(uuid, new AttributeSource(CLASS_PREFIX + c.getName(), attributeData, true));
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
        AttributeSource removed = mgr().removeSource(uuid, CLASS_PREFIX + c.getName());
        return removed != null ? removed.getData() : null;
    }

    /**
     * 清除插件关联的所有实体属性数据
     *
     * @param c Class
     */
    public void removePluginAllEntityData(Class<?> c) {
        String name = CLASS_PREFIX + c.getName();
        for (UUID uuid : mgr().getTrackedEntities()) {
            mgr().removeSource(uuid, name);
        }
    }

    /**
     * 清除该实体所有 Class 分源数据
     *
     * @param uuid 实体UUID
     */
    public void removeEntityAllPluginData(UUID uuid) {
        for (String name : mgr().getSourceNames(uuid)) {
            if (name.startsWith(CLASS_PREFIX)) {
                mgr().removeSource(uuid, name);
            }
        }
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
        return SXAttribute.getConditionManager().isUse(entity, type, null, list);
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
        if (SXAttribute.isHigherVersion()) {
            AttributeInstance instance = AttributeUtil.getInstance(entity, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
            if (instance != null) return instance.getBaseValue();
        }
        // 1.8 或高版本解析失败时回退到 Bukkit 老 API
        return entity.getMaxHealth();
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
