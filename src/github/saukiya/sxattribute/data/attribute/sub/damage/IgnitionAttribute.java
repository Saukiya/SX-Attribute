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
 * 点燃
 *
 * @author Saukiya
 */
public class IgnitionAttribute extends SubAttribute {

    /**
     * double[0] 点燃几率
     */
    public IgnitionAttribute() {
        super("Ignition", 1, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            DamageEventData damageEventData = (DamageEventData) eventData;
            if (getAttributes()[0] > 0 && probability(getAttributes()[0] - damageEventData.getEntityAttributeDoubles("Toughness")[0])) {
                damageEventData.getEntity().setFireTicks(40 + SXAttribute.getRandom().nextInt(60));
                damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__IGNITION, getDf().format(damageEventData.getEntity().getFireTicks() / 20D)));
                Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__IGNITION, damageEventData.getEntityName(), getFirstPerson(), getDf().format(damageEventData.getEntity().getFireTicks() / 20D));
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__IGNITION, getFirstPerson(), damageEventData.getDamagerName(), getDf().format(damageEventData.getEntity().getFireTicks() / 20D));
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Ignition") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Ignition");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_IGNITION))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_IGNITION);
    }
}
