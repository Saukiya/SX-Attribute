package github.saukiya.sxattribute.feature.equipment;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.event.SXLoadAttributeEvent;
import github.saukiya.sxattribute.event.SXPreLoadItemEvent;
import github.saukiya.sxattribute.event.SXForgeTransactionEvent;
import github.saukiya.sxattribute.feature.equipment.affix.AffixFeature;
import github.saukiya.sxattribute.feature.equipment.core.AggregateEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.CostTransaction;
import github.saukiya.sxattribute.feature.equipment.core.EquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.FeatureItemState;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import github.saukiya.sxattribute.feature.equipment.enchantgrowth.EnchantGrowthFeature;
import github.saukiya.sxattribute.feature.equipment.enhance.EnhanceFeature;
import github.saukiya.sxattribute.feature.equipment.quality.QualityFeature;
import github.saukiya.sxattribute.feature.equipment.reforge.ReforgeFeature;
import github.saukiya.sxattribute.feature.equipment.reroll.RerollFeature;
import github.saukiya.sxattribute.feature.equipment.setbonus.SetBonusFeature;
import github.saukiya.sxattribute.feature.equipment.socket.SocketFeature;
import github.saukiya.sxattribute.feature.equipment.star.StarFeature;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxitem.SXItem;
import github.saukiya.sxitem.event.SXItemUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 独立装备模块注册器与统一箱子 GUI。
 * <p>
 * GUI 会话按玩家加锁，操作前校验并扣除完整成本；状态提交后统一刷新 SX-Item 锁变量。
 */
public class ForgeFeatureManager implements Listener {

    /**
     * 装备拓展 Lore 的显示专用前缀。
     * {@code SXAttributeManager} 会在该标记处截断属性文本；放在行首后客户端仍可展示后续内容，
     * 但 Lore 与 Rendered NBT 都不会再次参与属性累计。
     */
    private static final String DISPLAY_ONLY_LORE_PREFIX = "§X";

    /**
     * SX-Item 锁变量名的固定前缀。变量名不能含点号，因为 SX-Item NBT 包装器会把点号解释为节点分隔符。
     */
    private static final String LOCK_VARIABLE_PREFIX = "SXAttribute_";

    /** 空模块使用 SX-Item 的删行协议，避免可选变量在 Lore 中留下空白行。 */
    private static final String EMPTY_LOCK_VALUE = "$<DeleteLore>";

    private final Map<String, EquipmentFeature> features = new LinkedHashMap<>();
    private final Map<UUID, ForgeSession> sessions = new ConcurrentHashMap<>();
    private YamlConfiguration forgeGui;
    private YamlConfiguration forgeMessages;

    public ForgeFeatureManager() {
        AffixFeature affix = register(new AffixFeature());
        register(new SetBonusFeature());
        register(new QualityFeature());
        register(new EnhanceFeature());
        register(new StarFeature());
        register(new ReforgeFeature(affix));
        register(new RerollFeature(affix));
        register(new SocketFeature());
        register(new EnchantGrowthFeature());
        loadForgeConfig();
        Bukkit.getPluginManager().registerEvents(this, SXAttribute.getInst());
        for (EquipmentFeature feature : features.values()) {
            if (feature instanceof Listener) Bukkit.getPluginManager().registerEvents((Listener) feature, SXAttribute.getInst());
        }
    }

    private <T extends EquipmentFeature> T register(T feature) {
        try {
            feature.reload();
            features.put(feature.id().toLowerCase(), feature);
            return feature;
        } catch (RuntimeException | LinkageError exception) {
            SXAttribute.getInst().getLogger().severe("Equipment feature " + feature.id()
                    + " was disabled without affecting SX-Attribute: " + exception.getClass().getSimpleName()
                    + ": " + exception.getMessage());
            return null;
        }
    }

    public void reload() {
        for (EquipmentFeature feature : new ArrayList<>(features.values())) {
            try {
                feature.reload();
            } catch (RuntimeException | LinkageError exception) {
                features.remove(feature.id().toLowerCase());
                SXAttribute.getInst().getLogger().severe("Equipment feature " + feature.id()
                        + " failed during reload and was disabled: " + exception.getClass().getSimpleName()
                        + ": " + exception.getMessage());
            }
        }
        loadForgeConfig();
    }

    private void loadForgeConfig() {
        File directory = new File(SXAttribute.getInst().getDataFolder(), "Feature" + File.separator + "Forge");
        File gui = new File(directory, "Gui.yml");
        File messages = new File(directory, "Messages.yml");
        if (!gui.exists()) SXAttribute.getInst().saveResource("Feature/Forge/Gui.yml", false);
        if (!messages.exists()) SXAttribute.getInst().saveResource("Feature/Forge/Messages.yml", false);
        forgeGui = YamlConfiguration.loadConfiguration(gui);
        forgeMessages = YamlConfiguration.loadConfiguration(messages);
    }

    public EquipmentFeature feature(String id) {
        return id == null ? null : features.get(id.toLowerCase());
    }

    public List<String> enabledFeatureIds() {
        List<String> ids = new ArrayList<>();
        features.values().stream().filter(EquipmentFeature::enabled).forEach(feature -> ids.add(feature.id()));
        return ids;
    }

    /**
     * API 直接执行模块状态转换；成本由调用方负责，结果写入 NBT 后刷新 SX-Item 锁变量。
     */
    public FeatureResult operate(Player player, ItemStack item, String featureId) {
        EquipmentFeature feature = feature(featureId);
        if (feature == null || !feature.enabled()) return FeatureResult.unsupported("Feature disabled: " + featureId);
        YamlConfiguration state = FeatureItemState.read(item, feature.id());
        FeatureResult result = feature.apply(player, item, state);
        if (result.isChanged()) FeatureItemState.write(item, feature.id(), state);
        if (!result.isDestroy()) refreshFeatureDisplay(player, item, true);
        return result;
    }

    /** 读取模块 NBT 状态快照，供外部 GUI 或任务系统展示。 */
    public YamlConfiguration state(ItemStack item, String featureId) {
        return FeatureItemState.read(item, featureId);
    }

    /** 写入模块 NBT 状态并立即刷新 SX-Item 锁变量。 */
    public void state(Player player, ItemStack item, String featureId, YamlConfiguration state) {
        FeatureItemState.write(item, featureId, state);
        refreshFeatureDisplay(player, item, true);
    }

    public void open(Player player) {
        open(player, null);
    }

    /** 打开全部功能或仅展示指定模块的锻造界面。 */
    public void open(Player player, String selectedFeature) {
        ForgeHolder holder = new ForgeHolder();
        int size = Math.max(9, Math.min(54, forgeGui.getInt("Size", 27) / 9 * 9));
        Inventory inventory = Bukkit.createInventory(holder, size, forgeGui.getString("Title", "&8SX-Attribute 锻造").replace('&', '§'));
        holder.inventory = inventory;
        int slot = 0;
        Map<Integer, String> slots = new LinkedHashMap<>();
        for (EquipmentFeature feature : features.values()) {
            if (!feature.enabled()) continue;
            if (selectedFeature != null && !feature.id().equalsIgnoreCase(selectedFeature)) continue;
            int configuredSlot = feature.gui().getInt("Slot", feature.config().getInt("Gui.Slot", slot++));
            inventory.setItem(configuredSlot, icon(feature));
            slots.put(configuredSlot, feature.id());
        }
        ItemStack held = SXAttribute.isHigherVersion() ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        sessions.put(player.getUniqueId(), new ForgeSession(slots, fingerprint(held)));
        player.openInventory(inventory);
    }

    private ItemStack icon(EquipmentFeature feature) {
        Material material = Material.matchMaterial(feature.gui().getString("Material", feature.config().getString("Gui.Material", "ANVIL")));
        ItemStack item = new ItemStack(material == null ? Material.ANVIL : material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(feature.gui().getString("Name", feature.config().getString("Gui.Name", "&a" + feature.id())).replace('&', '§'));
        List<String> lore = feature.gui().getStringList("Lore");
        if (lore.isEmpty()) lore = forgeGui.getStringList("DefaultLore");
        lore.replaceAll(line -> line.replace('&', '§'));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreLoad(SXPreLoadItemEvent event) {
        Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        event.getItemList().forEach(item -> refreshFeatureDisplay(player, item.getItem(), false));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLoad(SXLoadAttributeEvent event) {
        for (EquipmentFeature feature : features.values()) {
            if (feature.enabled() && feature instanceof AggregateEquipmentFeature) {
                ((AggregateEquipmentFeature) feature).contribute(event);
            }
        }
    }

    /**
     * SX-Item 更新模板时迁移作为真实数据源的模块 State。LORE 模式在新物品上直接重建显示文本；
     * VARIABLE 模式由 SX-Item 在生成新物品时解析 {@code <l:SXAttribute_<模块>_Lore>}。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSXItemUpdate(SXItemUpdateEvent event) {
        for (EquipmentFeature feature : features.values()) {
            FeatureItemState.synchronize(event.getOldItem(), event.getItem(), feature.id());
        }
        if (Config.getEquipmentFeatureLoreMode() == Config.EquipmentFeatureLoreMode.LORE) {
            renderAll(event.getPlayer(), event.getItem());
        } else {
            // 新物品已经由 SX-Item 模板生成；这里只补齐 State 派生的 Attributes 与下一次更新所需的 Lock。
            synchronizeSXItemVariables(event.getPlayer(), event.getItem(), false, false);
        }
    }

    /** 根据配置把显示权交给直接 Lore 渲染或 SX-Item 锁变量，两条路径不会同时执行。 */
    private void refreshFeatureDisplay(Player player, ItemStack item, boolean updateItem) {
        if (Config.getEquipmentFeatureLoreMode() == Config.EquipmentFeatureLoreMode.VARIABLE) {
            synchronizeSXItemVariables(player, item, updateItem);
        } else {
            renderDirectLore(player, item);
        }
    }

    /**
     * LORE 模式会先把旧 VARIABLE 模式留下的锁变量置为删行值，并借 SX-Item 重建一次模板；
     * 没有旧变量或不是 SX-Item 时直接渲染，避免两种模式的显示同时残留。
     */
    private void renderDirectLore(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        boolean clearedVariable = false;
        if (SXItem.getItemManager().getGenerator(item) != null) {
            for (EquipmentFeature feature : features.values()) {
                String path = lockPath(feature);
                String value = SXAttribute.getNbtUtil().getNBT(item, path);
                if (value != null && !EMPTY_LOCK_VALUE.equals(value) && !"<DeleteLore>".equals(value)) {
                    SXAttribute.getNbtUtil().setNBT(item, path, EMPTY_LOCK_VALUE);
                    clearedVariable = true;
                }
            }
        }
        if (clearedVariable) {
            // 更新事件会在新物品上调用 renderAll；这里不能再渲染一次，否则会重复添加显示行。
            SXItem.getItemManager().updateItem(player, item);
        } else {
            renderAll(player, item);
        }
    }

    /** LORE 模式统一重建所有模块显示，并用 Rendered NBT 精确移除上一次生成的行。 */
    private void renderAll(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        for (EquipmentFeature feature : features.values()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.removeAll(FeatureItemState.rendered(item, feature.id()));
            YamlConfiguration state = FeatureItemState.read(item, feature.id());
            List<String> attributes = feature.enabled()
                    ? feature.render(player, item, state) : Collections.emptyList();
            FeatureItemState.attributes(item, feature.id(), attributes);
            List<String> rendered = markDisplayOnly(attributes);
            lore.addAll(rendered);
            meta.setLore(lore);
            item.setItemMeta(meta);
            FeatureItemState.rendered(item, feature.id(), rendered);
        }
    }

    /**
     * 将每个模块的显示文本写入 SX-Item.Lock，并可选地请求 SX-Item 按物品模板重新生成物品。
     * 每个变量是换行分隔的完整 Lore 块，SX-Item 会按自身列表展开协议拆成多行。
     */
    private void synchronizeSXItemVariables(Player player, ItemStack item, boolean updateItem) {
        synchronizeSXItemVariables(player, item, updateItem, true);
    }

    /**
     * @param migrateDirectLore 是否允许检测旧 Rendered 后再次调用 SX-Item 更新；事件回调内必须关闭以避免递归更新
     */
    private void synchronizeSXItemVariables(Player player, ItemStack item, boolean updateItem, boolean migrateDirectLore) {
        if (item == null || item.getType() == Material.AIR) return;
        if (SXItem.getItemManager().getGenerator(item) == null) return;

        boolean legacyDirectLoreFound = false;
        for (EquipmentFeature feature : features.values()) {
            // 旧版本曾把渲染结果直接写进 ItemMeta；检测到旧 Rendered 时借 SX-Item 重建完成一次性迁移。
            legacyDirectLoreFound |= !FeatureItemState.rendered(item, feature.id()).isEmpty();
            YamlConfiguration state = FeatureItemState.read(item, feature.id());
            List<String> attributes = feature.enabled()
                    ? feature.render(player, item, state) : Collections.emptyList();
            FeatureItemState.attributes(item, feature.id(), attributes);
            List<String> rendered = markDisplayOnly(attributes);
            String value = rendered.isEmpty() ? EMPTY_LOCK_VALUE : String.join("\n", rendered);
            SXAttribute.getNbtUtil().setNBT(item, lockPath(feature), value);
        }
        if (updateItem || (migrateDirectLore && legacyDirectLoreFound)) {
            SXItem.getItemManager().updateItem(player, item);
        }
    }

    /** Lock NBT 路径必须与公开给 SX-Item 模板的变量名严格一致。 */
    private String lockPath(EquipmentFeature feature) {
        return SXItem.getInst().getName() + ".Lock." + lockVariable(feature);
    }

    /** 模块 ID 中的点号同样需要转义，确保整个变量名始终是 SX-Item.Lock 下的单一键。 */
    private String lockVariable(EquipmentFeature feature) {
        return LOCK_VARIABLE_PREFIX + feature.id().replace('.', '_') + "_Lore";
    }

    /**
     * 为模块生成的全部 Lore 添加显示专用协议标记。
     * 已带标记的行保持不变，避免外部模块主动使用该协议时被重复添加。
     */
    private List<String> markDisplayOnly(List<String> lines) {
        List<String> marked = new ArrayList<>(lines.size());
        for (String line : lines) {
            marked.add(line == null || line.startsWith(DISPLAY_ONLY_LORE_PREFIX)
                    ? line : DISPLAY_ONLY_LORE_PREFIX + line);
        }
        return marked;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ForgeHolder) || !(event.getWhoClicked() instanceof Player)) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) return;
        Player player = (Player) event.getWhoClicked();
        ForgeSession session = sessions.get(player.getUniqueId());
        if (session == null || session.busy) return;
        String id = session.slots.get(event.getRawSlot());
        EquipmentFeature feature = feature(id);
        if (feature == null || !feature.enabled()) return;
        ItemStack item = SXAttribute.isHigherVersion() ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(message("NoItem", "&c请先在主手持有装备"));
            return;
        }
        if (session.itemFingerprint != fingerprint(item)) {
            player.sendMessage(message("ItemChanged", "&c主手装备已变化，请重新打开锻造界面"));
            return;
        }
        session.busy = true;
        CostTransaction transaction = CostTransaction.create(player, feature.config().getConfigurationSection("Cost"));
        if (!transaction.charge(feature.config().getConfigurationSection("Cost"))) {
            player.sendMessage(message("CostInsufficient", "&c消耗不足"));
            session.busy = false;
            return;
        }
        ItemStack[] inventorySnapshot = cloneContents(player.getInventory().getContents());
        ItemStack offHandSnapshot = SXAttribute.isHigherVersion() && player.getInventory().getItemInOffHand() != null
                ? player.getInventory().getItemInOffHand().clone() : null;
        try {
            YamlConfiguration state = FeatureItemState.read(item, feature.id());
            FeatureResult result = feature.apply(player, item, state);
            if (!result.isAccepted()) transaction.rollback();
            if (result.isChanged()) FeatureItemState.write(item, feature.id(), state);
            if (result.isDestroy()) {
                if (SXAttribute.isHigherVersion()) player.getInventory().setItemInMainHand(null);
                else player.setItemInHand(null);
            } else {
                refreshFeatureDisplay(player, item, true);
            }
            player.sendMessage(result.getMessage());
            Bukkit.getPluginManager().callEvent(new SXForgeTransactionEvent(player, feature.id(), item, result));
            SXAttribute.getAttributeManager().loadEntityData(player);
            SXAttribute.getAttributeManager().attributeUpdateEvent(player);
            session.itemFingerprint = result.isDestroy() ? 0 : fingerprint(item);
        } catch (RuntimeException exception) {
            player.getInventory().setContents(inventorySnapshot);
            if (SXAttribute.isHigherVersion()) player.getInventory().setItemInOffHand(offHandSnapshot);
            transaction.rollback();
            player.sendMessage(message("TransactionFailed", "&c操作失败，消耗已返还"));
            SXAttribute.getInst().getLogger().warning("Forge transaction failed: " + exception.getMessage());
        } finally {
            session.busy = false;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof ForgeHolder) sessions.remove(event.getPlayer().getUniqueId());
    }

    private static final class ForgeHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private static final class ForgeSession {
        private final Map<Integer, String> slots;
        private boolean busy;
        private int itemFingerprint;

        private ForgeSession(Map<Integer, String> slots, int itemFingerprint) {
            this.slots = slots;
            this.itemFingerprint = itemFingerprint;
        }
    }

    /** 绑定 GUI 打开时的完整 ItemStack 状态，防止热键换物后误操作另一件装备。 */
    private int fingerprint(ItemStack item) {
        return item == null ? 0 : item.hashCode();
    }

    private ItemStack[] cloneContents(ItemStack[] contents) {
        ItemStack[] snapshot = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            snapshot[index] = contents[index] == null ? null : contents[index].clone();
        }
        return snapshot;
    }

    private String message(String key, String fallback) {
        return forgeMessages.getString(key, fallback).replace('&', '§');
    }
}
