package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * 防御力
 *
 * @author Saukiya
 */
public class DefenseAttribute extends SubAttribute {

    /**
     * double[0] 防御最小值
     * double[1] 防御最大值
     * double[2] 防御最小值 - PVP
     * double[3] 防御最大值 - PVP
     * double[4] 防御最小值 - PVE
     * double[5] 防御最大值 - PVE
     */
    public DefenseAttribute() {
        super("Defense", 6, SXAttributeType.DEFENCE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            DamageEventData damageEventData = (DamageEventData) eventData;
            if (!damageEventData.getEffectiveAttributeList().contains("Real")) {
                damageEventData.takeDamage(getAttribute());
                if (damageEventData.getDamager() instanceof Player) {
                    damageEventData.takeDamage(getPVPAttribute());
                } else {
                    damageEventData.takeDamage(getPVEAttribute());
                }
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        if (string.equalsIgnoreCase("MinDefense")) {
            return getDf().format(getAttributes()[0]);
        } else if (string.equalsIgnoreCase("MaxDefense")) {
            return getDf().format(getAttributes()[1]);
        } else if (string.equalsIgnoreCase("Defense")) {
            return getAttributes()[0] == getAttributes()[1] ? getDf().format(getAttributes()[0]) : (getDf().format(getAttributes()[0]) + " - " + getDf().format(getAttributes()[1]));
        } else if (string.equalsIgnoreCase("PvpMinDefense")) {
            return getDf().format(getAttributes()[2]);
        } else if (string.equalsIgnoreCase("PvpMaxDefense")) {
            return getDf().format(getAttributes()[3]);
        } else if (string.equalsIgnoreCase("PvpDefense")) {
            return getAttributes()[2] == getAttributes()[3] ? getDf().format(getAttributes()[2]) : (getDf().format(getAttributes()[2]) + " - " + getDf().format(getAttributes()[3]));
        } else if (string.equalsIgnoreCase("PveMinDefense")) {
            return getDf().format(getAttributes()[4]);
        } else if (string.equalsIgnoreCase("PveMaxDefense")) {
            return getDf().format(getAttributes()[5]);
        } else if (string.equalsIgnoreCase("PveDefense")) {
            return getAttributes()[4] == getAttributes()[5] ? getDf().format(getAttributes()[4]) : (getDf().format(getAttributes()[4]) + " - " + getDf().format(getAttributes()[5]));
        } else {
            return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("MinDefense", "MaxDefense", "Defense", "PvpMinDefense", "PvpMaxDefense", "PvpDefense", "PveMinDefense", "PveMaxDefense", "PveDefense");
    }

    private double getAttribute() {
        return getAttributes()[0] + SXAttribute.getRandom().nextDouble() * (getAttributes()[1] - getAttributes()[0]);
    }

    private double getPVPAttribute() {
        return getAttributes()[2] + SXAttribute.getRandom().nextDouble() * (getAttributes()[3] - getAttributes()[2]);
    }

    private double getPVEAttribute() {
        return getAttributes()[4] + SXAttribute.getRandom().nextDouble() * (getAttributes()[5] - getAttributes()[4]);
    }

    @Override
    public boolean loadAttribute(String lore) {
        String[] loreSplit = lore.split("-");
        if (lore.contains(Config.getConfig().getString(Config.NAME_PVE_DEFENSE))) {
            this.getAttributes()[4] += Double.valueOf(getNumber(loreSplit[0]));
            this.getAttributes()[5] += Double.valueOf(getNumber(loreSplit.length > 1 ? loreSplit[1] : loreSplit[0]));
        } else if (lore.contains(Config.getConfig().getString(Config.NAME_PVP_DEFENSE))) {
            this.getAttributes()[2] += Double.valueOf(getNumber(loreSplit[0]));
            this.getAttributes()[3] += Double.valueOf(getNumber(loreSplit.length > 1 ? loreSplit[1] : loreSplit[0]));
        } else if (lore.contains(Config.getConfig().getString(Config.NAME_DEFENSE))) {
            this.getAttributes()[0] += Double.valueOf(getNumber(loreSplit[0]));
            this.getAttributes()[1] += Double.valueOf(getNumber(loreSplit.length > 1 ? loreSplit[1] : loreSplit[0]));
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void correct() {
        if (getAttributes()[0] <= 0) getAttributes()[0] = 0D;
        if (getAttributes()[1] < getAttributes()[0]) getAttributes()[1] = getAttributes()[0];
        if (getAttributes()[2] <= 0) getAttributes()[2] = 0D;
        if (getAttributes()[3] < getAttributes()[2]) getAttributes()[3] = getAttributes()[2];
        if (getAttributes()[4] <= 0) getAttributes()[4] = 0D;
        if (getAttributes()[5] < getAttributes()[4]) getAttributes()[5] = getAttributes()[4];
    }

    @Override
    public double getValue() {
        double value = (getAttributes()[0] + getAttributes()[1]) / 2 * Config.getConfig().getInt(Config.VALUE_DEFENSE);
        value += (getAttributes()[2] + getAttributes()[3]) / 2 * Config.getConfig().getInt(Config.VALUE_PVP_DEFENSE);
        value += (getAttributes()[4] + getAttributes()[5]) / 2 * Config.getConfig().getInt(Config.VALUE_PVE_DEFENSE);
        return value;
    }
}
