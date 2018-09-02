package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 闪避
 *
 * @author Saukiya
 */
public class DodgeAttribute extends SubAttribute {

    /**
     * double[0] 闪避几率
     */
    public DodgeAttribute() {
        super("Dodge", 1, SXAttributeType.DEFENCE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            DamageEventData damageEventData = (DamageEventData) eventData;
            if (getAttributes()[0] > 0 && probability(getAttributes()[0] - damageEventData.getDamagerAttributeDoubles("HitRate")[0])) {
                damageEventData.setCancelled(true);
                Location loc = damageEventData.getDamager().getLocation().clone();
                loc.setYaw(loc.getYaw() + SXAttribute.getRandom().nextInt(80) - 40);
                damageEventData.getEntity().setVelocity(loc.getDirection().setY(0.1).multiply(0.7));
                damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__DODGE));
                Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__DODGE, damageEventData.getEntityName(), getFirstPerson());
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__DODGE, getFirstPerson(), damageEventData.getDamagerName());
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Dodge") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Dodge");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_DODGE))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_DODGE);
    }
}
