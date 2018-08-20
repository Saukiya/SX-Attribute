package github.saukiya.sxattribute.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Saukiya
 * @since 2018年3月25日
 */

public class OnDamageListener implements Listener {

    @Getter
    private final List<Hologram> hologramsList;

    private final SXAttribute plugin;

    public OnDamageListener(SXAttribute plugin) {
        this.plugin = plugin;
        if (SXAttribute.isHolographic()) {
            hologramsList = new ArrayList<>();
        } else {
            hologramsList = null;
        }
    }

    @EventHandler
    void onProjectileHitEvent(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        Entity projectile = event.getProjectile();
        LivingEntity entity = event.getEntity();
        plugin.getAttributeManager().setProjectileData(projectile.getUniqueId(), plugin.getAttributeManager().getEntityData(entity));
        ItemStack item = event.getBow();
        if (item != null && OnItemDurabilityListener.getUnbreakable(item.getItemMeta())) {
            plugin.getOnItemDurabilityListener().takeDurability(entity, item, 1, false);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        LivingEntity entity = null;
        LivingEntity damager = null;
        SXAttributeData entityData;
        SXAttributeData damagerData = null;
        // 当攻击者为投抛物时
        if (event.getDamager() instanceof Projectile) {
            Projectile arrow = (Projectile) event.getDamager();
            if (arrow.getShooter() instanceof LivingEntity) {
                damagerData = plugin.getAttributeManager().getProjectileData(arrow.getUniqueId());
                damager = (LivingEntity) arrow.getShooter();
            }
        } else if (event.getDamager() instanceof LivingEntity) {
            damager = (LivingEntity) event.getDamager();
        }
        if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand)) {
            entity = (LivingEntity) event.getEntity();
        }
        // 若有一方为null 或 怪v怪的属性计算 则取消
        if (entity == null || damager == null || (Config.isDamageCalculationToEVE() && !(entity instanceof Player || damager instanceof Player))) {
            return;
        }

        entityData = plugin.getAttributeManager().getEntityData(entity);
        damagerData = damagerData != null ? damagerData : plugin.getAttributeManager().getEntityData(damager);

        // 主手持弓左键判断
        if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
            EntityEquipment eq = damager.getEquipment();
            ItemStack mainHand = eq.getItemInMainHand();
            if (mainHand != null) {
                if (Material.BOW.equals(mainHand.getType())) {
                    event.setDamage(1);
                    damagerData = plugin.getAttributeManager().getEntityData(damager, new SXAttributeData());
                }
                if (!Material.AIR.equals(mainHand.getType()) && mainHand.getItemMeta().hasLore()) {
                    if (damager instanceof Player && !((HumanEntity) damager).getGameMode().equals(GameMode.CREATIVE)) {
                        if (mainHand.getType().getMaxDurability() == 0 || OnItemDurabilityListener.getUnbreakable(mainHand.getItemMeta())) {
                            plugin.getOnItemDurabilityListener().takeDurability(damager, mainHand, 1, false);
                        }
                    }
                }
            }
        }

        String entityName = plugin.getOnHealthChangeDisplayListener().getEntityName(entity, entity.getName());
        String damagerName = plugin.getOnHealthChangeDisplayListener().getEntityName(damager, damager.getName());

        DamageEventData damageEventData = new DamageEventData(entity, damager, entityName, damagerName, entityData, damagerData, event);

        for (Map.Entry<Integer, SubAttribute> entry : entityData.getAttributeMap().entrySet()) {
            if (entry.getValue().containsType(SXAttributeType.DAMAGE)) {
                damagerData.getAttributeMap().get(entry.getKey()).eventMethod(damageEventData);
            } else if (entry.getValue().containsType(SXAttributeType.DEFENCE)) {
                entry.getValue().eventMethod(damageEventData);
            }
            if (damageEventData.isCancelled() || damageEventData.getDamage() <= 0) {
                event.setCancelled(true);
                break;
            }
        }
        event.setDamage(damageEventData.getDamage());

        if (!event.isCancelled()) {
            damageEventData.sendHolo(Message.getMsg(damageEventData.isCrit() && event.getDamage() > 0 ? Message.PLAYER__HOLOGRAPHIC__CRIT : Message.PLAYER__HOLOGRAPHIC__DAMAGE, SXAttribute.getDf().format(event.getFinalDamage())));
        }

        if (Config.isHolographic() && SXAttribute.isHolographic() && !event.getCause().equals(DamageCause.ENTITY_SWEEP_ATTACK)) {
            Location loc = entity.getEyeLocation().clone().add(0, 0.6 - SXAttribute.getRandom().nextDouble() / 2, 0);
            loc.setYaw(damager.getLocation().getYaw() + 90);
            loc.add(loc.getDirection().multiply(0.8D));
            Hologram hologram = HologramsAPI.createHologram(SXAttribute.getPlugin(), loc);
            for (String message : damageEventData.getHoloList()) {
                hologram.appendTextLine(message);
            }
            hologramsList.add(hologram);
            new BukkitRunnable() {
                @Override
                public void run() {
                    hologram.delete();
                    hologramsList.remove(hologram);
                }
            }.runTaskLater(SXAttribute.getPlugin(), Config.getConfig().getInt(Config.HOLOGRAPHIC_DISPLAY_TIME));
        }
    }
}
