package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.api.DamageAPI;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.sxattribute.event.SXDamageEvent;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxitem.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author Saukiya
 */

public class ListenerDamage implements Listener {

    @EventHandler
    void onProjectileHitEvent(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        Entity projectile = event.getProjectile();
        LivingEntity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            SXAttribute.getApi().setProjectileData(projectile.getUniqueId(), SXAttribute.getAttributeManager().getEntityData(entity));
            ItemStack item = event.getBow();
            if (item != null && SubCondition.isUnbreakable(item.getItemMeta())) {
                Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent((Player) entity, item, 1));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || Config.getDamageEventBlackList().contains(event.getCause().name())) {
            return;
        }
        LivingEntity defenseEntity = (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand)) ? (LivingEntity) event.getEntity() : null;
        LivingEntity attackEntity = null;
        SXAttributeData defenseData;
        SXAttributeData attackData = null;
        // 当攻击者为投抛物时
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
            attackEntity = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
            attackData = SXAttribute.getApi().getProjectileData(event.getDamager().getUniqueId());
        } else if (event.getDamager() instanceof LivingEntity) {
            attackEntity = (LivingEntity) event.getDamager();
        }

        // 若有一方为null 或 怪v怪的属性计算 则取消
        if (defenseEntity == null || attackEntity == null || (!Config.isDamageCalculationToEVE() && !(defenseEntity instanceof Player || attackEntity instanceof Player))) {
            return;
        }

        defenseData = SXAttribute.getAttributeManager().getEntityData(defenseEntity);
        attackData = attackData != null ? attackData : SXAttribute.getAttributeManager().getEntityData(attackEntity);

        EntityEquipment eq = attackEntity.getEquipment();
        ItemStack mainHand = NMS.compareTo(1, 9, 0) >= 0 ? eq.getItemInMainHand() : eq.getItemInHand();
        if (mainHand != null) {
            if (!Material.AIR.equals(mainHand.getType()) && mainHand.getItemMeta().hasLore()) {
                if (attackEntity instanceof Player && !((HumanEntity) attackEntity).getGameMode().equals(GameMode.CREATIVE)) {
                    if (mainHand.getType().getMaxDurability() == 0 || SubCondition.isUnbreakable(mainHand.getItemMeta())) {
                        Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent((Player) attackEntity, mainHand, 1));
                    }
                }
            }
        }

        String defenseName = SXAttribute.getListenerHealthChange().getEntityName(defenseEntity);
        UUID defenseUUID = defenseEntity.getUniqueId();
        String attackName = SXAttribute.getListenerHealthChange().getEntityName(attackEntity);
        UUID attackUUID = attackEntity.getUniqueId();


        SXAttributeData apiDamageAttribute = DamageAPI.getDamageData(attackUUID, defenseUUID);
        boolean apiDamage = false;
        if (apiDamageAttribute != null) {
            attackData = apiDamageAttribute;
            apiDamage = true;
        }

        DamageData damageData = new DamageData(defenseEntity, attackEntity, defenseName, attackName, defenseData, attackData, event);

        // 如果造成伤害来自于API 则会进行判断豁免
        damageData.setFromAPI(apiDamage);

        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            if (attribute.containsType(AttributeType.ATTACK) && attackData.isValid(attribute)) {
                System.out.println("Join-A: " + attribute.getName() + ", " + Arrays.toString(attackData.getValues(attribute)) + ", " + damageData);
                attribute.eventMethod(attackData.getValues(attribute), damageData);
            } else if (attribute.containsType(AttributeType.DEFENCE) && defenseData.isValid(attribute)) {
                System.out.println("Join-D: " + attribute.getName() + ", " + Arrays.toString(defenseData.getValues(attribute)) + ", " + damageData);
                attribute.eventMethod(defenseData.getValues(attribute), damageData);
            }

            if (damageData.isCancelled() || damageData.getDamage() <= 0) {
                damageData.setDamage(Config.getMinimumDamage());
                break;
            }
        }
//        damageData.setDamage(damageData.getDamage() > Config.getMinimumDamage() ? damageData.getDamage() : Config.getMinimumDamage(), "All");
        Bukkit.getPluginManager().callEvent(new SXDamageEvent(damageData));

        if (apiDamage) {
            // 将伤害属性修正回来 触发需要一次伤害 减少一点即可
            damageData.setDamage(damageData.getDamage() - 1.0, "All");
            // 清空伤害属性
            DamageAPI.removeByCaster(attackUUID);
        }
    }
}
