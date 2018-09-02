package github.saukiya.sxattribute.data.attribute.sub.damage;

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
 * 破甲
 *
 * @author Saukiya
 */
public class RealAttribute extends SubAttribute {

    /**
     * double[0] 破甲几率
     */
    public RealAttribute() {
        super("Real", 1, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            if (probability(getAttributes()[0])) {
                DamageEventData damageEventData = (DamageEventData) eventData;
                damageEventData.getEffectiveAttributeList().add(this.getName());
                damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__REAL));
                Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__REAL, damageEventData.getEntityName(), getFirstPerson());
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__REAL, getFirstPerson(), damageEventData.getDamagerName());
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Real") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Real");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_REAL))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_REAL);
    }
}
