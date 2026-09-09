package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.sxattribute.event.SXDamageEvent;
import github.saukiya.sxattribute.feature.attribute.AttributeDefinition;
import github.saukiya.sxattribute.feature.attribute.AttributeExecutionContext;
import github.saukiya.sxattribute.hook.mythic.SkillDamageContext;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.AttributeConfig;
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

/**
 * @author Saukiya
 */

public class ListenerDamage implements Listener {

    /** LOWEST 尽早绑定技能事件；HIGH 按对象身份读取，且仍尊重其它监听器已经发生的取消。 */
    @EventHandler(priority = EventPriority.LOWEST)
    public void captureSkillDamage(EntityDamageByEntityEvent event) {
        SkillDamageContext.capture(event);
    }

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
        SkillDamageContext skill = SkillDamageContext.find(event);
        if (event.isCancelled()) return;
        if (Config.getDamageEventBlackList().contains(event.getCause().name())) {
            // 技能不能在跳过属性计算后悄悄退化成无防御的普通数值伤害。
            if (skill != null) event.setCancelled(true);
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

        // EVE 开关只控制自动普攻计算；显式 SX 技能允许怪物间交战，仍不能伤害盔甲架。
        if (defenseEntity == null || attackEntity == null || (skill == null && !Config.isDamageCalculationToEVE() && !(defenseEntity instanceof Player || attackEntity instanceof Player))) {
            if (skill != null) event.setCancelled(true);
            return;
        }

        defenseData = SXAttribute.getAttributeManager().getEntityData(defenseEntity);
        // 技能已经准备了独立攻击快照，不重复发布一次读取攻击者属性的事件。
        attackData = skill != null ? skill.getAttributes()
                : attackData != null ? attackData : SXAttribute.getAttributeManager().getEntityData(attackEntity);

        EntityEquipment eq = attackEntity.getEquipment();
        ItemStack mainHand = eq == null ? null : SXAttribute.isHigherVersion() ? eq.getItemInMainHand() : eq.getItemInHand();
        // 显式施法不消耗一次普通近战的武器耐久。
        if (skill == null && mainHand != null) {
            if (!Material.AIR.equals(mainHand.getType()) && mainHand.getItemMeta().hasLore()) {
                if (attackEntity instanceof Player && !((HumanEntity) attackEntity).getGameMode().equals(GameMode.CREATIVE)) {
                    if (mainHand.getType().getMaxDurability() == 0 || SubCondition.isUnbreakable(mainHand.getItemMeta())) {
                        Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent((Player) attackEntity, mainHand, 1));
                    }
                }
            }
        }

        String defenseName = SXAttribute.getListenerHealthChange().getEntityName(defenseEntity);
        String attackName = SXAttribute.getListenerHealthChange().getEntityName(attackEntity);

        DamageData damageData = new DamageData(defenseEntity, attackEntity, defenseName, attackName, defenseData, attackData, event);
        if (skill != null) damageData.setSkillContext(skill);

        AttributeExecutionContext dynamicContext = new AttributeExecutionContext(attackEntity, defenseEntity, attackData, defenseData, damageData);
        if (SXAttribute.getAttributeEngine() != null) {
            SXAttribute.getAttributeEngine().fire(AttributeDefinition.AttributeTrigger.DAMAGE_ATTACK, dynamicContext);
            SXAttribute.getAttributeEngine().fire(AttributeDefinition.AttributeTrigger.DAMAGE_DEFEND, dynamicContext);
        }

        boolean damageReached = false;
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            // 自定义 CANCEL 必须在内置吸血/药水等副作用之前停止。
            if (skill != null && damageData.isCancelled()) break;
            if (!AttributeConfig.isEnabled(attribute.getName())) continue;
            if ("Damage".equals(attribute.getName())) damageReached = true;
            if (attribute.containsType(AttributeType.ATTACK) && attackData.isValid(attribute)) {
                attribute.eventMethod(attackData.getValues(attribute), damageData);
            } else if (attribute.containsType(AttributeType.DEFENCE) && defenseData.isValid(attribute)) {
                attribute.eventMethod(defenseData.getValues(attribute), damageData);
            }

            if (damageData.isCancelled() || damageData.getDamage() <= 0) {
                // a=0 的属性技能在 Dodge/JSAttribute 之后仍需进入 Damage，不能被提前补成最小伤害。
                if (skill != null && !damageData.isCancelled() && !damageReached) continue;
                damageData.setDamage(Config.getMinimumDamage());
                break;
            }
        }
        damageData.setDamage(damageData.getDamage() > Config.getMinimumDamage() ? damageData.getDamage() : Config.getMinimumDamage());
        if (skill != null) {
            // dm 作用于 SX 攻防流程的结果，Bukkit 护甲/吸收等修正继续由原生事件处理。
            double damage = damageData.getDamage() * skill.getMultiplier();
            if (!Double.isFinite(damage) || damage <= 0D) damageData.setCancelled(true);
            damageData.setDamage(Double.isFinite(damage) ? Math.max(0D, damage) : 0D);
        }
        Bukkit.getPluginManager().callEvent(new SXDamageEvent(damageData));
        if (SXAttribute.getAttributeEngine() != null) {
            dynamicContext.getVariables().put("event_damage", damageData.getDamage());
            dynamicContext.getVariables().put("event_cancelled", damageData.isCancelled() ? 1D : 0D);
            SXAttribute.getAttributeEngine().fire(AttributeDefinition.AttributeTrigger.DAMAGE_AFTER, dynamicContext);
        }
    }
}
