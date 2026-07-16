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
 * GUI 会话按玩家加锁，操作前校验并扣除完整成本；状态提交后统一重建所有模块 Lore。
 */
public class ForgeFeatureManager implements Listener {

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
        feature.reload();
        features.put(feature.id().toLowerCase(), feature);
        return feature;
    }

    public void reload() {
        features.values().forEach(EquipmentFeature::reload);
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
     * API 直接执行模块状态转换；成本由调用方负责，结果仍会写 NBT 并重建 Lore。
     */
    public FeatureResult operate(Player player, ItemStack item, String featureId) {
        EquipmentFeature feature = feature(featureId);
        if (feature == null || !feature.enabled()) return FeatureResult.unsupported("Feature disabled: " + featureId);
        YamlConfiguration state = FeatureItemState.read(item, feature.id());
        FeatureResult result = feature.apply(player, item, state);
        if (result.isChanged()) FeatureItemState.write(item, feature.id(), state);
        if (!result.isDestroy()) renderAll(player, item);
        return result;
    }

    /** 读取模块 NBT 状态快照，供外部 GUI 或任务系统展示。 */
    public YamlConfiguration state(ItemStack item, String featureId) {
        return FeatureItemState.read(item, featureId);
    }

    /** 写入模块 NBT 状态并立即重建 Lore。 */
    public void state(Player player, ItemStack item, String featureId, YamlConfiguration state) {
        FeatureItemState.write(item, featureId, state);
        renderAll(player, item);
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
        event.getItemList().forEach(item -> renderAll(player, item.getItem()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLoad(SXLoadAttributeEvent event) {
        for (EquipmentFeature feature : features.values()) {
            if (feature.enabled() && feature instanceof AggregateEquipmentFeature) {
                ((AggregateEquipmentFeature) feature).contribute(event);
            }
        }
    }

    public void renderAll(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        for (EquipmentFeature feature : features.values()) renderFeature(player, item, feature);
    }

    private void renderFeature(Player player, ItemStack item, EquipmentFeature feature) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeAll(FeatureItemState.rendered(item, feature.id()));
        YamlConfiguration state = FeatureItemState.read(item, feature.id());
        List<String> rendered = feature.enabled() ? feature.render(player, item, state) : Collections.emptyList();
        lore.addAll(rendered);
        meta.setLore(lore);
        item.setItemMeta(meta);
        FeatureItemState.rendered(item, feature.id(), rendered);
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
                renderAll(player, item);
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
