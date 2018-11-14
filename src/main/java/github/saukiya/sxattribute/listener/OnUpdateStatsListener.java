package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
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

public class OnUpdateStatsListener implements Listener {

    private final SXAttribute plugin;

    public OnUpdateStatsListener(SXAttribute plugin) {
        this.plugin = plugin;
    }

    /**
     * 更新手中的物品
     *
     * @param player   Player
     * @param itemList ItemStack[]
     */
    private void updateHandData(Player player, ItemStack... itemList) {
        if (itemList.length > 0) {
            int i = 0;
            for (ItemStack item : itemList) {
                if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
                    i = i + 1;
                }
            }
            if (i == itemList.length) return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAttributeManager().loadHandData(player);
                plugin.getAttributeManager().updateStatsEvent(player);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * 更新装备栏、手中、饰品的物品
     *
     * @param player Player
     */
    private void updateEquipmentData(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAttributeManager().loadSlotData(player);
                plugin.getAttributeManager().loadEquipmentData(player);
                plugin.getAttributeManager().loadRPGInventoryData(player);
                plugin.getAttributeManager().loadHandData(player);
                plugin.getAttributeManager().updateStatsEvent(player);
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Inventory inv = player.getInventory();
        ItemStack oldItem = inv.getItem(event.getPreviousSlot());
        ItemStack newItem = inv.getItem(event.getNewSlot());
        updateHandData(player, oldItem, newItem);
    }

    @EventHandler
    void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack oldItem = event.getMainHandItem();
        ItemStack newItem = event.getOffHandItem();
        updateHandData(player, oldItem, newItem);
    }

    @EventHandler
    void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        if (SXAttribute.isRpgInventory()) {
            updateEquipmentData(player);
        } else {
            if (inv.getName().contains("container") || inv.getName().contains("Repair")) {
                updateEquipmentData(player);
            }
        }
    }

    @EventHandler
    void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        updateHandData(player, item);
    }

    @EventHandler
    void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        updateHandData(player, item);
    }

    @EventHandler
    void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
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

    @EventHandler
    void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateEquipmentData(player);
    }

    @EventHandler
    void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getAttributeManager().clearEntityData(player.getUniqueId());
    }

    @EventHandler
    void onEntitySpawnEvent(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        entity.setInvulnerable(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (entity != null && !entity.isDead()) {
                    plugin.getAttributeManager().loadHandData(entity);
                    plugin.getAttributeManager().loadEquipmentData(entity);
                    plugin.getAttributeManager().updateStatsEvent(entity);
                    entity.setInvulnerable(false);
                }
            }
        }.runTaskLaterAsynchronously(plugin, 16);
    }

    @EventHandler
    void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            plugin.getAttributeManager().clearEntityData(event.getEntity().getUniqueId());
        }
    }
}
