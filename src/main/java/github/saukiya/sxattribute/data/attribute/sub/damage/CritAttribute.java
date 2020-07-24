package github.saukiya.sxattribute.data.attribute.sub.damage;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * 暴击
 *
 * @author Saukiya
 */
public class CritAttribute extends SubAttribute {

    /**
     * double[0] 暴击几率
     * double[1] 暴击伤害
     */
    public CritAttribute() {
        super("Crit", 2, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            if (probability(getAttributes()[0])) {
                DamageEventData damageEventData = (DamageEventData) eventData;
                damageEventData.setCrit(true);
                damageEventData.setDamage(damageEventData.getDamage() * getAttributes()[1] / 100);
                Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__CRIT, getFirstPerson(), damageEventData.getEntityName());
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__CRIT, damageEventData.getDamagerName(), getFirstPerson());
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        if (string.equalsIgnoreCase("CritRate")) {
            return getDf().format(getAttributes()[0]);
        } else if (string.equalsIgnoreCase("Crit")) {
            return getDf().format(getAttributes()[1]);
        } else {
            return null;
        }
    }

    @Override
    public List<String> introduction() {
        return Arrays.asList(
                "Chinese: 暴击",
                "判断几率造成暴击,默认伤害为100%(原伤害)",
                "",
                "English: Crit",
                "Determine the chance to cause a crit",
                " the default damage is 100% (original damage)"
        );
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("CritRate", "Crit");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_CRIT_RATE))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        if (lore.contains(Config.getConfig().getString(Config.NAME_CRIT))) {
            getAttributes()[1] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public void correct() {
        if (getAttributes()[0] < 0) getAttributes()[0] = 0D;
        if (getAttributes()[1] < 100) getAttributes()[1] = 100D;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_CRIT_RATE) + getAttributes()[1] * Config.getConfig().getInt(Config.VALUE_CRIT);
    }
}
