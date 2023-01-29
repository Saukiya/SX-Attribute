package github.saukiya.sxattribute.data.attribute;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.data.SlotData;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.event.SXGetAttributeEvent;
import github.saukiya.sxattribute.event.SXLoadAttributeEvent;
import github.saukiya.sxattribute.event.SXPreLoadItemEvent;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxitem.SXItem;
import github.saukiya.sxitem.util.NMS;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.inventory.InventoryManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 属性管理器-onLoad方法勿调用
 *
 * @author Saukiya
 */
public class SXAttributeManager implements Listener {

    @Getter
    private final Map<UUID, SXAttributeData> entityDataMap = new ConcurrentHashMap<>();

    private SXAttributeData defaultAttributeData;

    public SXAttributeManager() {
        Bukkit.getPluginManager().registerEvents(this, SXAttribute.getInst());
        int size = SubAttribute.getAttributes().size();
        Collections.sort(SubAttribute.getAttributes());
        for (int i = 0; i < size; i++) {
            SubAttribute.getAttributes().get(i).setPriority(i).loadConfig().onEnable();
        }
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
    }

    /**
     * 获取物品的属性
     *
     * @param entity      实体
     * @param preItemList 预加载物品
     * @return SXAttribute
     */
    public SXAttributeData loadItemData(LivingEntity entity, List<PreLoadItem> preItemList) {
        Iterator<PreLoadItem> iterator = preItemList.iterator();
        while (iterator.hasNext()) {
            PreLoadItem preLoadItem = iterator.next();
            List<String> list = new ArrayList<>();
            if (preLoadItem.getItem().getItemMeta().hasLore()) {
                list = preLoadItem.getItem().getItemMeta().getLore().stream().map(str -> str.split("§X")[0]).filter(str -> str.length() > 0).collect(Collectors.toList());
            }
            if (!SXAttribute.getConditionManager().isUse(entity, preLoadItem.getType(), list)) {
                iterator.remove();
            }
        }

        //CallEvent
        Bukkit.getPluginManager().callEvent(new SXPreLoadItemEvent(entity, preItemList));

        SXAttributeData attributeData = new SXAttributeData();
        // LoadAttribute
        for (PreLoadItem preLoadItem : preItemList) {
            if (preLoadItem.getItem().getItemMeta().hasLore()) {
                attributeData.add(loadListData(preLoadItem.getItem().getItemMeta().getLore().stream().map(str -> str.split("§X")[0]).filter(str -> str.length() > 0).collect(Collectors.toList())));
            }
        }

        //CallEvent
        Bukkit.getPluginManager().callEvent(new SXLoadAttributeEvent(entity, preItemList, attributeData));
        return attributeData;
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
        Bukkit.getScheduler().runTask(SXAttribute.getInst(), () -> {
            UpdateData updateData = new UpdateData(entity);
            SXAttributeData attributeData = getEntityData(entity);
            for (SubAttribute attribute : SubAttribute.getAttributes()) {
                if (attribute.containsType(AttributeType.UPDATE)) {
                    attribute.eventMethod(attributeData.getValues()[attribute.getPriority()], updateData);
                }
            }
        });
    }

    public void loadDefaultAttributeData() {
        defaultAttributeData = loadListData(Config.getConfig().getStringList(Config.DEFAULT_ATTRIBUTE));
    }

    /**
     * 获取生物总数据
     *
     * @param entity LivingEntity
     * @return SXAttributeData
     */
    public SXAttributeData getEntityData(LivingEntity entity) {
        SXAttributeData data = new SXAttributeData();
        data.add(getEntityDataMap().get(entity.getUniqueId()));
        data.add(SXAttribute.getApi().getAPIAttribute(entity.getUniqueId()));
        data.calculationCombatPower();
        data.add(defaultAttributeData);
        SXGetAttributeEvent event = new SXGetAttributeEvent(entity, data);
        Bukkit.getPluginManager().callEvent(event);
        data.correct();
        return data;
    }

    /**
     * 清除生物数据
     *
     * @param uuid EntityUUID
     */
    public void clearEntityData(UUID uuid) {
        getEntityDataMap().remove(uuid);
        SXAttribute.getApi().removeEntityAllPluginData(uuid);
    }

    /**
     * 加载生物数据
     *
     * @param entity LivingEntity
     */
    public void loadEntityData(LivingEntity entity) {
        Player player = entity instanceof Player ? (Player) entity : null;
        List<PreLoadItem> preItemList = new ArrayList<>();

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
                    SXItem.getItemManager().updateItem((Player) entity, item);
                    if (item != null && !item.getType().equals(Material.AIR) && item.getItemMeta().hasLore() && item.getItemMeta().getLore().stream().anyMatch(lore -> lore.contains(slotData.getName()))) {
                        preItemList.add(new PreLoadItem(EquipmentType.SLOT, item));
                    }
                }
            }

            // Equipment Load
            for (ItemStack item : entity.getEquipment().getArmorContents()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    preItemList.add(new PreLoadItem(EquipmentType.EQUIPMENT, item));
                }
            }
        }

        // Hand Load
        if (entity.getEquipment().getItemInHand() != null && !entity.getEquipment().getItemInHand().getType().equals(Material.AIR)) {
            preItemList.add(new PreLoadItem(EquipmentType.MAIN_HAND, entity.getEquipment().getItemInHand()));
        }
        if (NMS.compareTo(1,9,0) >= 0) {
            if (entity.getEquipment().getItemInOffHand() != null && !entity.getEquipment().getItemInOffHand().getType().equals(Material.AIR)) {
                preItemList.add(new PreLoadItem(EquipmentType.OFF_HAND, entity.getEquipment().getItemInOffHand()));
            }
        }

        // Update Items
        if (player != null) {
            for (PreLoadItem preLoadItem : preItemList) {
                SXItem.getItemManager().updateItem(player, preLoadItem.getItem());
            }
        }

        SXAttributeData attributeData = loadItemData(entity, preItemList);

        if (attributeData.isValid()) {
            entityDataMap.put(entity.getUniqueId(), attributeData);
        } else {
            entityDataMap.remove(entity.getUniqueId());
        }
    }
}