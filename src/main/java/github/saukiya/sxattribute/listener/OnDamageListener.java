package github.saukiya.sxattribute.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.event.SXDamageEvent;
import github.saukiya.sxattribute.util.Config;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Saukiya
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
        if (entity instanceof Player) {
            plugin.getAttributeManager().setProjectileData(projectile.getUniqueId(), plugin.getAttributeManager().getEntityData(entity));
            ItemStack item = event.getBow();
            if (item != null && SubCondition.getUnbreakable(item.getItemMeta())) {
                Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent((Player) entity, item, 1));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || event.getCause().equals(DamageCause.CUSTOM)) {
            return;
        }
        LivingEntity entity = (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand)) ? (LivingEntity) event.getEntity() : null;
        LivingEntity damager = null;
        SXAttributeData entityData;
        SXAttributeData damagerData = null;
        // 当攻击者为投抛物时
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
            damager = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
            damagerData = plugin.getAttributeManager().getProjectileData(event.getDamager().getUniqueId());
        } else if (event.getDamager() instanceof LivingEntity) {
            damager = (LivingEntity) event.getDamager();
        }
        // 若有一方为null 或 怪v怪的属性计算 则取消
        if (entity == null || damager == null || (!Config.isDamageCalculationToEVE() && !(entity instanceof Player || damager instanceof Player))) {
            return;
        }

        entityData = plugin.getAttributeManager().getEntityData(entity);
        damagerData = damagerData != null ? damagerData : plugin.getAttributeManager().getEntityData(damager);

        if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
            EntityEquipment eq = damager.getEquipment();
            ItemStack mainHand = eq.getItemInMainHand();
            if (mainHand != null) {
                // 主手持弓左键判断
                if (Material.BOW.equals(mainHand.getType())) {
                    event.setDamage(1);
                    damagerData = plugin.getAttributeManager().getEntityData(damager, new SXAttributeData[]{null});
                }
                if (!Material.AIR.equals(mainHand.getType()) && mainHand.getItemMeta().hasLore()) {
                    if (damager instanceof Player && !((HumanEntity) damager).getGameMode().equals(GameMode.CREATIVE)) {
                        if (mainHand.getType().getMaxDurability() == 0 || SubCondition.getUnbreakable(mainHand.getItemMeta())) {
                            Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent((Player) damager, mainHand, 1));
                        }
                    }
                }
            }
        }

        String entityName = plugin.getOnHealthChangeDisplayListener().getEntityName(entity);
        String damagerName = plugin.getOnHealthChangeDisplayListener().getEntityName(damager);

        DamageEventData damageEventData = new DamageEventData(entity, damager, entityName, damagerName, entityData, damagerData, event);


        //双Map迭代
        Iterator<SubAttribute> damagerIterator = damagerData.getAttributeMap().values().iterator();
        Iterator<SubAttribute> entityIterator = entityData.getAttributeMap().values().iterator();

        while (damagerIterator.hasNext() && entityIterator.hasNext()) {
            SubAttribute damageAttribute = damagerIterator.next();
            SubAttribute entityAttribute = entityIterator.next();
            if (damageAttribute.containsType(SXAttributeType.DAMAGE) && Arrays.stream(damageAttribute.getAttributes()).anyMatch(d -> d != 0)) {
                damageAttribute.eventMethod(damageEventData);
            } else if (entityAttribute.containsType(SXAttributeType.DEFENCE) && Arrays.stream(entityAttribute.getAttributes()).anyMatch(d -> d != 0)) {
                entityAttribute.eventMethod(damageEventData);
            }
            if (damageEventData.isCancelled() || damageEventData.getDamage() <= 0) {
                event.setCancelled(damageEventData.isCancelled());
                damageEventData.setDamage(0.1D);
                break;
            }
        }

        event.setDamage(damageEventData.getDamage());

        /**
         * 事件处理Event消息
         * 发送一个事件 内部存储DamageEventData
         */
        Bukkit.getPluginManager().callEvent(new SXDamageEvent(damageEventData));
    }
}
