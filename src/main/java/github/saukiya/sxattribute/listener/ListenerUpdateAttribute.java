package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.sub.RepairCommand;
import github.saukiya.sxattribute.command.sub.SellCommand;
import github.saukiya.sxattribute.util.Util;
import github.saukiya.tools.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
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

import java.util.Arrays;

public class ListenerUpdateAttribute implements Listener {

    public ListenerUpdateAttribute() {
        if (NMS.compareTo(1,9,0) >= 0) {
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
        if (itemList.length > 0 && Arrays.stream(itemList).allMatch(item -> item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())) {
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
        new BukkitRunnable() {
            @Override
            public void run() {
                SXAttribute.getAttributeManager().loadEntityData(player);
                SXAttribute.getAttributeManager().attributeUpdateEvent(player);
            }
        }.runTaskAsynchronously(SXAttribute.getInst());
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
            if (inv.getHolder().equals(player) || inv.getHolder().equals(RepairCommand.holder) || inv.getHolder().equals(SellCommand.holder)) {
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

    @EventHandler(ignoreCancelled = true)
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

    @EventHandler
    void onPlayerJoinEvent(PlayerJoinEvent event) {
        updateEquipmentData(event.getPlayer());
    }

    @EventHandler
    void onPlayerQuitEvent(PlayerQuitEvent event) {
        SXAttribute.getAttributeManager().clearEntityData(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    void onEntitySpawnEvent(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        if (NMS.compareTo(1,9,0) >= 0) {
            entity.setInvulnerable(true);
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(SXAttribute.getInst(), () -> {
            if (entity != null && !entity.isDead()) {
                SXAttribute.getAttributeManager().loadEntityData(entity);
                SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
                if (NMS.compareTo(1,9,0) >= 0) {
                    entity.setInvulnerable(false);
                }
            }
        }, 10);
    }

    @EventHandler
    void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Util.info(" >The Arrow is Death");
            if (SXAttribute.getAttributeManager().getEntityDataMap().containsKey(event.getEntity().getUniqueId())) {
                Util.info("  >this has Attribute");
                YamlConfiguration yaml;
            }
        }
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
