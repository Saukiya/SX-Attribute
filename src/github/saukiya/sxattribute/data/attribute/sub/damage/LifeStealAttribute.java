package github.saukiya.sxattribute.data.attribute.sub.damage;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.listener.OnHealthChangeDisplayListener;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.Arrays;
import java.util.List;

/**
 * 吸血
 *
 * @author Saukiya
 */
public class LifeStealAttribute extends SubAttribute {

    /**
     * double[0] 吸血几率
     * double[1] 吸血倍率
     */
    public LifeStealAttribute() {
        super("LifeSteal", 2, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            if (probability(getAttributes()[0])) {
                DamageEventData damageEventData = (DamageEventData) eventData;
                LivingEntity damager = damageEventData.getDamager();
                double maxHealth = OnHealthChangeDisplayListener.getMaxHealth(damager);
                double lifeHealth = damageEventData.getDamage() * getAttributes()[1] / 100;
                EntityRegainHealthEvent event = new EntityRegainHealthEvent(damager, lifeHealth, EntityRegainHealthEvent.RegainReason.CUSTOM);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                lifeHealth = (maxHealth < damager.getHealth() + event.getAmount()) ? (maxHealth - damager.getHealth()) : event.getAmount();
                damager.setHealth(damager.getHealth() + lifeHealth);
                damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__LIFE_STEAL, getDf().format(lifeHealth)));
                Message.send(damager, Message.PLAYER__BATTLE__LIFE_STEAL, damageEventData.getEntityName(), getFirstPerson(), getDf().format(lifeHealth));
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__LIFE_STEAL, getFirstPerson(), damageEventData.getDamagerName(), getDf().format(lifeHealth));
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("LifeStealRate") ? getDf().format(getAttributes()[0]) : string.equalsIgnoreCase("LifeSteal") ? getDf().format(getAttributes()[1]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("LifeStealRate", "LifeSteal");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_LIFE_STEAL_RATE))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        if (lore.contains(Config.getConfig().getString(Config.NAME_LIFE_STEAL))) {
            getAttributes()[1] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_LIFE_STEAL_RATE) + getAttributes()[1] * Config.getConfig().getInt(Config.VALUE_LIFE_STEAL);
    }
}
