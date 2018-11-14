package github.saukiya.sxattribute.data.attribute.sub.damage;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 雷霆
 *
 * @author Saukiya
 */
public class LightningAttribute extends SubAttribute {

    /**
     * double[0] 雷霆几率
     */
    public LightningAttribute() {
        super("Lightning", 1, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            DamageEventData damageEventData = (DamageEventData) eventData;
            if (getAttributes()[0] > 0 && probability(getAttributes()[0] - damageEventData.getEntityAttributeDoubles("Toughness")[0])) {
                damageEventData.getEntity().getWorld().strikeLightningEffect(damageEventData.getEntity().getLocation());
                double lightningDamage = damageEventData.getEntity().getHealth() * SXAttribute.getRandom().nextDouble() / 10;
                damageEventData.getEntity().setHealth(damageEventData.getEntity().getHealth() - lightningDamage);
                damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__LIGHTNING, getDf().format(lightningDamage)));
                Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__LIGHTNING, damageEventData.getEntityName(), getFirstPerson(), getDf().format(lightningDamage));
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__LIGHTNING, getFirstPerson(), damageEventData.getDamagerName(), getDf().format(lightningDamage));
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Lightning") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Lightning");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_LIGHTNING))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_LIGHTNING);
    }
}
