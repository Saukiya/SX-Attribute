package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Arrays;
import java.util.List;

/**
 * 反射
 *
 * @author Saukiya
 */
public class ReflectionAttribute extends SubAttribute {

    /**
     * double[0] 反射几率
     * double[1] 反射伤害
     */
    public ReflectionAttribute() {
        super("Reflection", 2, SXAttributeType.DEFENCE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            if (probability(getAttributes()[0])) {
                DamageEventData damageEventData = (DamageEventData) eventData;
                if (!(damageEventData.getEffectiveAttributeList().contains("Real") || damageEventData.getEffectiveAttributeList().contains("Block"))) {
                    damageEventData.getEffectiveAttributeList().add(this.getName());
                    double damage = damageEventData.getDamage() * getAttributes()[1] / 100;
                    LivingEntity damager = damageEventData.getDamager();
                    EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damageEventData.getEntity(), damager, EntityDamageEvent.DamageCause.CUSTOM, damage);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    damager.playEffect(EntityEffect.HURT);
                    damager.setHealth(damager.getHealth() < event.getFinalDamage() ? 0 : (damager.getHealth() - event.getFinalDamage()));
                    damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__REFLECTION, getDf().format(damage)));
                    Message.send(damager, Message.PLAYER__BATTLE__REFLECTION, getFirstPerson(), damageEventData.getEntityName(), getDf().format(damage));
                    Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__REFLECTION, damageEventData.getDamagerName(), getFirstPerson(), getDf().format(damage));
                }
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        if (string.equalsIgnoreCase("ReflectionRate")) {
            return getDf().format(getAttributes()[0]);
        } else if (string.equalsIgnoreCase("Reflection")) {
            return getDf().format(getAttributes()[1]);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("ReflectionRate", "Reflection");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_REFLECTION_RATE))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        if (lore.contains(Config.getConfig().getString(Config.NAME_REFLECTION))) {
            getAttributes()[1] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_REFLECTION_RATE) + getAttributes()[1] * Config.getConfig().getInt(Config.VALUE_REFLECTION);
    }
}
