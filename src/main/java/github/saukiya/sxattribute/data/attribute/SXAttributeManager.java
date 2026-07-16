package github.saukiya.sxattribute.data.attribute;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.data.SlotData;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.event.SXAttributeSourceAddEvent;
import github.saukiya.sxattribute.event.SXAttributeSourceRemoveEvent;
import github.saukiya.sxattribute.event.SXGetAttributeEvent;
import github.saukiya.sxattribute.event.SXLoadAttributeEvent;
import github.saukiya.sxattribute.event.SXPreLoadItemEvent;
import github.saukiya.sxattribute.util.AttributeConfig;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.Bukkit;
import github.saukiya.sxattribute.util.FoliaScheduler;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.inventory.InventoryManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 属性管理器 - onLoad方法勿调用
 * <p>
 * 多属性源: 每个实体持有 {@code Map<String, AttributeSource>}(唯一真源), 最终属性 = 全部命名源之和。
 * 内部物品按 {@link EquipmentType} 分组为命名源(装备-主手/副手/盔甲/槽位/RPG栏); 外部脚本/插件按任意名;
 * 旧 Class 分源与抛射物由 {@code SXAPI} 映射为保留名({@code class:<全类名>} / {@code 抛射物})委托到此。
 *
 * @author Saukiya
 */
public class SXAttributeManager implements Listener {

    /** 内部物品源保留名(按 EquipmentType 分组) */
    public static final String SOURCE_MAIN_HAND = "装备-主手";
    public static final String SOURCE_OFF_HAND = "装备-副手";
    public static final String SOURCE_EQUIPMENT = "装备-盔甲";
    public static final String SOURCE_SLOT = "槽位";
    public static final String SOURCE_RPG = "RPG栏";
    /** SXLoadAttributeEvent 监听器就地注入的增量(保证与旧版聚合行为等价) */
    public static final String SOURCE_ITEM_EXTRA = "物品-附加";

    /** 由 loadEntityData 管理、每次重载会清除重建的内部物品源集合 */
    private static final Set<String> ITEM_SOURCE_NAMES = new HashSet<>(Arrays.asList(
            SOURCE_MAIN_HAND, SOURCE_OFF_HAND, SOURCE_EQUIPMENT, SOURCE_SLOT, SOURCE_RPG, SOURCE_ITEM_EXTRA));

    /**
     * 唯一真源: 实体 -> (源名 -> 源)
     */
    private final Map<UUID, Map<String, AttributeSource>> entitySources = new ConcurrentHashMap<>();

    private SXAttributeData defaultAttributeData;

    public SXAttributeManager() {
        Bukkit.getPluginManager().registerEvents(this, SXAttribute.getInst());
        int size = SubAttribute.getAttributes().size();
        Collections.sort(SubAttribute.getAttributes());
        for (int i = 0; i < size; i++) {
            SubAttribute.getAttributes().get(i).setPriority(i).loadConfig().onEnable();
        }
        AttributeConfig.saveIfDirty();
        SXAttribute.getInst().getLogger().info("Loaded " + size + " Attributes");
    }

    @EventHandler
    public void onSubAttributePluginEnableEvent(PluginEnableEvent event) {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            if (attribute.getPlugin().equals(event.getPlugin()) && attribute instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) attribute, attribute.getPlugin());
            }
        }
        if (SubAttribute.getAttributes().stream().allMatch(sub -> sub.getPlugin().isEnabled())) {
            loadDefaultAttributeData();
        }
    }

    public void onAttributeDisable() {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            attribute.onDisable();
        }
    }

    public void onAttributeReload() {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            attribute.loadConfig().onReLoad();
        }
        AttributeConfig.saveIfDirty();
    }

    // ==================== 多属性源 底层原语 ====================

    /**
     * 写入/覆盖一个命名源 (同名替换, 不叠加)
     */
    public void putSource(UUID uuid, AttributeSource source) {
        entitySources.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(source.getName(), source);
    }

    /**
     * 移除一个命名源, 返回被移除的源 (无则 null)
     */
    public AttributeSource removeSource(UUID uuid, String name) {
        Map<String, AttributeSource> map = entitySources.get(uuid);
        return map != null ? map.remove(name) : null;
    }

    public AttributeSource getSource(UUID uuid, String name) {
        Map<String, AttributeSource> map = entitySources.get(uuid);
        return map != null ? map.get(name) : null;
    }

    public boolean hasSource(UUID uuid, String name) {
        return getSource(uuid, name) != null;
    }

    public Set<String> getSourceNames(UUID uuid) {
        Map<String, AttributeSource> map = entitySources.get(uuid);
        return map != null ? new HashSet<>(map.keySet()) : Collections.emptySet();
    }

    /**
     * 汇总实体全部命名源的数据
     */
    public SXAttributeData sumSources(UUID uuid) {
        SXAttributeData data = new SXAttributeData();
        Map<String, AttributeSource> map = entitySources.get(uuid);
        if (map != null) {
            for (AttributeSource source : map.values()) {
                data.add(source.getData());
            }
        }
        return data;
    }

    /**
     * 清除实体全部命名源
     */
    public void clearSources(UUID uuid) {
        entitySources.remove(uuid);
    }

    /**
     * 仅清除内部物品源(装备/手持/槽位/RPG), 保留外部脚本/插件/抛射物源 —— 独立生命周期
     */
    public void clearItemSources(UUID uuid) {
        Map<String, AttributeSource> map = entitySources.get(uuid);
        if (map != null) {
            ITEM_SOURCE_NAMES.forEach(map::remove);
        }
    }

    /**
     * @return 当前被追踪(持有任意源)的实体 UUID 集合
     */
    public Set<UUID> getTrackedEntities() {
        return entitySources.keySet();
    }

    /**
     * 添加命名源 (非静态且 callEvent 时发可取消的 SXAttributeSourceAddEvent)
     *
     * @param silent    静态源: 不触发事件
     * @param callEvent 是否触发事件
     * @return 是否成功添加 (被事件取消则 false)
     */
    public boolean addSource(LivingEntity entity, String name, SXAttributeData data, boolean silent, boolean callEvent) {
        if (!silent && callEvent) {
            SXAttributeSourceAddEvent event = new SXAttributeSourceAddEvent(entity, name, data);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            data = event.getData();
        }
        putSource(entity.getUniqueId(), new AttributeSource(name, data, silent));
        return true;
    }

    /**
     * 移除命名源 (非静态且 callEvent 时发 SXAttributeSourceRemoveEvent)
     *
     * @return 被移除源的数据 (无则 null)
     */
    public SXAttributeData takeSource(LivingEntity entity, String name, boolean callEvent) {
        AttributeSource removed = removeSource(entity.getUniqueId(), name);
        if (removed == null) {
            return null;
        }
        if (callEvent && !removed.isSilent()) {
            Bukkit.getPluginManager().callEvent(new SXAttributeSourceRemoveEvent(entity, name, removed.getData()));
        }
        return removed.getData();
    }

    /**
     * EquipmentType -> 内部物品源名
     */
    private static String itemSourceName(EquipmentType type) {
        switch (type) {
            case MAIN_HAND:
                return SOURCE_MAIN_HAND;
            case OFF_HAND:
                return SOURCE_OFF_HAND;
            case EQUIPMENT:
                return SOURCE_EQUIPMENT;
            case SLOT:
                return SOURCE_SLOT;
            case RPG_INVENTORY:
                return SOURCE_RPG;
            default:
                return SOURCE_ITEM_EXTRA;
        }
    }

    // ==================== 物品加载 ====================

    public SXAttributeData loadItemData(LivingEntity entity, List<PreLoadItem> preItemList) {
        return loadItemData(entity, preItemList, false);
    }

    /**
     * 获取物品的属性 (聚合, 兼容旧 API); 内部走 {@link #resolveItemSources} 分组求和
     *
     * @param entity      实体
     * @param preItemList 预加载物品
     * @return SXAttributeData 聚合
     */
    public SXAttributeData loadItemData(LivingEntity entity, List<PreLoadItem> preItemList, boolean isAsync) {
        SXAttributeData aggregate = new SXAttributeData();
        resolveItemSources(entity, preItemList, isAsync).values().forEach(aggregate::add);
        return aggregate;
    }

    /**
     * 条件过滤 + 触发 SXPreLoadItem/SXLoadAttribute 事件 + 按 EquipmentType 分组为命名源。
     * <p>
     * SXLoadAttributeEvent 监听器可就地修改聚合注入额外属性, 其增量被捕获为 {@link #SOURCE_ITEM_EXTRA} 源,
     * 保证"全部源之和 == 旧版聚合(含事件注入)"完全等价。
     *
     * @return 源名 -> 数据
     */
    private Map<String, SXAttributeData> resolveItemSources(LivingEntity entity, List<PreLoadItem> preItemList, boolean isAsync) {
        Iterator<PreLoadItem> iterator = preItemList.iterator();
        while (iterator.hasNext()) {
            PreLoadItem preLoadItem = iterator.next();
            List<String> list = new ArrayList<>();
            if (preLoadItem.getItem().getItemMeta().hasLore()) {
                list = preLoadItem.getItem().getItemMeta().getLore().stream().map(str -> str.split("§X")[0]).filter(str -> str.length() > 0).collect(Collectors.toList());
            }
            if (!SXAttribute.getConditionManager().isUse(entity, preLoadItem.getType(), preLoadItem.getItem(), list)) {
                iterator.remove();
            }
        }

        //CallEvent
        Bukkit.getPluginManager().callEvent(new SXPreLoadItemEvent(entity, preItemList, isAsync));

        Map<String, SXAttributeData> sources = new LinkedHashMap<>();
        SXAttributeData aggregate = new SXAttributeData();
        for (PreLoadItem preLoadItem : preItemList) {
            if (preLoadItem.getItem().getItemMeta().hasLore()) {
                SXAttributeData data = loadListData(preLoadItem.getItem().getItemMeta().getLore().stream().map(str -> str.split("§X")[0]).filter(str -> str.length() > 0).collect(Collectors.toList()));
                sources.computeIfAbsent(itemSourceName(preLoadItem.getType()), k -> new SXAttributeData()).add(data);
                aggregate.add(data);
            }
        }

        //CallEvent (监听器可就地修改 aggregate)
        Bukkit.getPluginManager().callEvent(new SXLoadAttributeEvent(entity, preItemList, aggregate, isAsync));

        // 捕获监听器注入的增量为独立源 (delta = 事件后聚合 - 分组求和)
        SXAttributeData preSum = new SXAttributeData();
        sources.values().forEach(preSum::add);
        SXAttributeData delta = new SXAttributeData().add(aggregate).take(preSum);
        if (delta.isValid()) {
            sources.put(SOURCE_ITEM_EXTRA, delta);
        }
        return sources;
    }

    /**
     * 获取Lore的属性
     * 带有§X 的一行不被识别属性
     *
     * @param list 物品lore，也可以是其他存有属性的list
     * @return SXAttributeData 不满足返回null
     */
    public SXAttributeData loadListData(List<String> list) {
        SXAttributeData sxAttributeData = new SXAttributeData();
        list.stream().map(str -> str.split("§X")[0]).filter(s -> s.length() > 0).forEach(s -> {
            for (SubAttribute attribute : SubAttribute.getAttributes()) {
                attribute.loadAttribute(sxAttributeData.getValues()[attribute.getPriority()], s);
            }
        });
        return sxAttributeData;
    }

    /**
     * 更新实体UPDATE类属性
     *
     * @param entity Player
     */
    public void attributeUpdateEvent(LivingEntity entity) {
        // 属性更新必须回到实体所属区域线程，否则 Folia 会拒绝修改实体状态。
        FoliaScheduler.runEntity(entity, SXAttribute.getInst(), () -> {
            UpdateData updateData = new UpdateData(entity);
            SXAttributeData attributeData = getEntityData(entity);
            for (SubAttribute attribute : SubAttribute.getAttributes()) {
                if (attribute.containsType(AttributeType.UPDATE)) {
                    attribute.eventMethod(attributeData.getValues()[attribute.getPriority()], updateData);
                }
            }
        }, 1);
    }

    public void loadDefaultAttributeData() {
        defaultAttributeData = loadListData(Config.getConfig().getStringList(Config.DEFAULT_ATTRIBUTE));
    }

    /**
     * @return 全局默认属性数据 (Config.DefaultAttribute; 作为独立"默认"来源展示, 可能为 null)
     */
    public SXAttributeData getDefaultAttributeData() {
        return defaultAttributeData;
    }

    /**
     * 获取生物总数据 = 全部命名源之和 + 默认属性
     *
     * @param entity LivingEntity
     * @return SXAttributeData
     */
    public SXAttributeData getEntityData(LivingEntity entity) {
        SXAttributeData data = new SXAttributeData();
        data.add(sumSources(entity.getUniqueId()));
        data.calculationCombatPower();
        data.add(defaultAttributeData);
        SXGetAttributeEvent event = new SXGetAttributeEvent(entity, data, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        data.correct();
        return data;
    }

    /**
     * 清除生物全部数据(所有命名源)
     *
     * @param uuid EntityUUID
     */
    public void clearEntityData(UUID uuid) {
        clearSources(uuid);
    }

    public void loadEntityData(LivingEntity entity) {
        loadEntityData(entity, false);
    }

    /**
     * 加载生物数据: 扫描装备/手持/槽位/RPG, 解析为按类型分组的内部物品命名源。
     * 仅重建内部物品源, 外部脚本/插件/抛射物源不受影响(独立生命周期)。
     *
     * @param entity LivingEntity
     */
    public void loadEntityData(LivingEntity entity, boolean isAsync) {
        Player player = entity instanceof Player ? (Player) entity : null;
        List<PreLoadItem> preItemList = new ArrayList<>();
        EntityEquipment equipment = entity.getEquipment();
        if (SXAttribute.isRpgInventory() && player != null) {
            // RPGInv Load
            Inventory inv = InventoryManager.get(player).getInventory();
            if (inv != null) {
                for (Integer index : Config.getRpgInvSlotList()) {
                    ItemStack item = inv.getItem(index);
                    if (item != null && !item.getType().equals(Material.AIR)) {
                        preItemList.add(new PreLoadItem(EquipmentType.RPG_INVENTORY, item));
                    }
                }
            }
        } else {

            // Slot Load
            if (player != null) {
                Inventory inv = player.getInventory();
                for (SlotData slotData : SXAttribute.getSlotDataManager().getSlotList()) {
                    ItemStack item = inv.getItem(slotData.getSlot());
                    SXAttribute.getItemDataManager().updateItem(item, (Player) entity);
                    if (item != null && !item.getType().equals(Material.AIR) && item.getItemMeta().hasLore() && item.getItemMeta().getLore().stream().anyMatch(lore -> lore.contains(slotData.getName()))) {
                        preItemList.add(new PreLoadItem(EquipmentType.SLOT, item));
                    }
                }
            }

            // Equipment Load
            if (equipment != null) {
                for (ItemStack item : equipment.getArmorContents()) {
                    if (item != null && !item.getType().equals(Material.AIR)) {
                        preItemList.add(new PreLoadItem(EquipmentType.EQUIPMENT, item));
                    }
                }
            }
        }

        // Hand Load
        if (equipment != null) {
            if (SXAttribute.isHigherVersion()) {
                if (equipment.getItemInMainHand() != null && !equipment.getItemInMainHand().getType().equals(Material.AIR)) {
                    preItemList.add(new PreLoadItem(EquipmentType.MAIN_HAND, equipment.getItemInMainHand()));
                }
                if (equipment.getItemInOffHand() != null && !equipment.getItemInOffHand().getType().equals(Material.AIR)) {
                    preItemList.add(new PreLoadItem(EquipmentType.OFF_HAND, equipment.getItemInOffHand()));
                }
            } else {
                if (equipment.getItemInHand() != null && !equipment.getItemInHand().getType().equals(Material.AIR)) {
                    preItemList.add(new PreLoadItem(EquipmentType.MAIN_HAND, equipment.getItemInHand()));
                }
            }
        }

        // Update Items
        if (player != null) {
            for (PreLoadItem preLoadItem : preItemList) {
                SXAttribute.getItemDataManager().updateItem(preLoadItem.getItem(), player);
            }
        }

        // 解析为分组命名源, 先清旧物品源再写入新的(外部源保留)
        Map<String, SXAttributeData> itemSources = resolveItemSources(entity, preItemList, isAsync);
        UUID uuid = entity.getUniqueId();
        clearItemSources(uuid);
        for (Map.Entry<String, SXAttributeData> entry : itemSources.entrySet()) {
            if (entry.getValue().isValid()) {
                putSource(uuid, new AttributeSource(entry.getKey(), entry.getValue(), true));
            }
        }
    }

    /**
     * 聚合只读快照 (兼容旧接口)。
     *
     * @deprecated 底层已改为多命名源, 本方法仅返回汇总快照; 对返回 Map 的 put/remove 不会影响真实数据,
     * 请改用 {@code SXAPI.addSourceAttribute/takeSourceAttribute} 或 {@link #putSource}/{@link #removeSource}。
     */
    @Deprecated
    public Map<UUID, SXAttributeData> getEntityDataMap() {
        Map<UUID, SXAttributeData> snapshot = new ConcurrentHashMap<>();
        for (UUID uuid : entitySources.keySet()) {
            snapshot.put(uuid, sumSources(uuid));
        }
        return snapshot;
    }
}
