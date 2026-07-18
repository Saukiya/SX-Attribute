package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.sub.RepairCommand;
import github.saukiya.sxattribute.command.sub.SellCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import github.saukiya.sxattribute.util.FoliaScheduler;

import java.util.Arrays;

public class ListenerUpdateAttribute implements Listener {

    public ListenerUpdateAttribute() {
        if (SXAttribute.isHigherVersion()) {
            Bukkit.getPluginManager().registerEvents(new VersionListener(), SXAttribute.getInst());
        }
    }

    /**
     * 更新手中的物品
     *
     * @param player   Player
     * @param itemList ItemStack[]
     */
    private void updateHandData(Player player, ItemStack... itemList) {
        // 纯 NBT 属性物品同样需要触发刷新，不能再只用 Lore 作为快速返回条件。
        if (itemList.length > 0 && Arrays.stream(itemList).noneMatch(SXAttribute.getAttributeManager()::hasItemAttributeData)) {
            return;
        }
        updateEquipmentData(player);
    }

    /**
     * 更新装备栏、手中、饰品的物品
     *
     * @param player Player
     */
    private void updateEquipmentData(Player player) {
        FoliaScheduler.runEntity(player, SXAttribute.getInst(), new BukkitRunnable() {
            @Override
            public void run() {
                SXAttribute.getAttributeManager().loadEntityData(player, true);
                SXAttribute.getAttributeManager().attributeUpdateEvent(player);
            }
        }, 1);
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        Inventory inv = event.getPlayer().getInventory();
        ItemStack oldItem = inv.getItem(event.getPreviousSlot());
        ItemStack newItem = inv.getItem(event.getNewSlot());
        updateHandData(event.getPlayer(), oldItem, newItem);
    }

    @EventHandler
    void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        if (SXAttribute.isRpgInventory()) {
            updateEquipmentData(player);
        } else {
            if (player.equals(inv.getHolder()) || RepairCommand.holder.equals(inv.getHolder()) || SellCommand.holder.equals(inv.getHolder())) {
                updateEquipmentData(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerDropEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        updateHandData(player, item);
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        updateHandData(player, item);
    }

    @EventHandler()
    void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((event.getAction() + "").contains("RIGHT")) {
            if (event.getItem() != null) {
                String name = event.getItem().getType().toString();
                if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS")) {
                    updateEquipmentData(player);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerDeathEvent(PlayerRespawnEvent event) {
        updateEquipmentData(event.getPlayer());
    }

    @EventHandler
    void onPlayerJoinEvent(PlayerJoinEvent event) {
        // 上线重新施加持久化源(跨重连存活), 再刷新装备源
        SXAttribute.getPersistentSourceManager().apply(event.getPlayer());
        updateEquipmentData(event.getPlayer());
    }

    @EventHandler
    void onPlayerQuitEvent(PlayerQuitEvent event) {
        SXAttribute.getAttributeManager().clearEntityData(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    void onEntitySpawnEvent(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        if (SXAttribute.isHigherVersion()) {
            entity.setInvulnerable(true);
        }
        FoliaScheduler.runEntity(entity, SXAttribute.getInst(), new BukkitRunnable() {
            @Override
            public void run() {
                if (entity != null && !entity.isDead()) {
                    SXAttribute.getAttributeManager().loadEntityData(entity, true);
                    SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
                    if (SXAttribute.isHigherVersion()) {
                        entity.setInvulnerable(false);
                    }
                }
            }
        }, 16);
    }

    @EventHandler
    void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            SXAttribute.getAttributeManager().clearEntityData(event.getEntity().getUniqueId());
        }
    }

    public class VersionListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
            Player player = event.getPlayer();
            ItemStack oldItem = event.getMainHandItem();
            ItemStack newItem = event.getOffHandItem();
            updateHandData(player, oldItem, newItem);
        }
    }
}
