package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * 防御力
 *
 * @author Saukiya
 */
public class Defense extends SubAttribute {

    @Getter
    private static int TYPE_DEFAULT = 0;
    @Getter
    private static int TYPE_PVP = 1;
    @Getter
    private static int TYPE_PVE = 2;

    /**
     * double[0] 防御最小值
     * double[1] 防御最大值
     * double[2] 防御最小值 - PVP
     * double[3] 防御最大值 - PVP
     * double[4] 防御最小值 - PVE
     * double[5] 防御最大值 - PVE
     */
    public Defense() {
        super(SXAttribute.getInst(), 6, AttributeType.DEFENCE);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Defense.DiscernName", "防御力");
        config.set("Defense.CombatPower", 1);
        config.set("PVPDefense.DiscernName", "PVP防御力");
        config.set("PVPDefense.CombatPower", 1);
        config.set("PVEDefense.DiscernName", "PVE防御力");
        config.set("PVEDefense.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            if (!damageData.getEffectiveAttributeList().contains("Real")) {
                damageData.takeDamage(getAttribute(values, TYPE_DEFAULT));
                damageData.takeDamage(getAttribute(values, damageData.getAttacker() instanceof Player ? TYPE_PVP : TYPE_PVE));
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        switch (string) {
            case "MinDefense":
                return values[0];
            case "MaxDefense":
                return values[1];
            case "Defense":
                return values[0] == values[1] ? values[0] : (getDf().format(values[0]) + " - " + getDf().format(values[1]));
            case "PvpMinDefense":
                return getDf().format(values[2]);
            case "PvpMaxDefense":
                return getDf().format(values[3]);
            case "PvpDefense":
                return values[2] == values[3] ? getDf().format(values[2]) : (getDf().format(values[2]) + " - " + getDf().format(values[3]));
            case "PveMinDefense":
                return getDf().format(values[4]);
            case "PveMaxDefense":
                return getDf().format(values[5]);
            case "PveDefense":
                return values[4] == values[5] ? getDf().format(values[4]) : (getDf().format(values[4]) + " - " + getDf().format(values[5]));
        }
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "MinDefense",
                "MaxDefense",
                "Defense",
                "PvpMinDefense",
                "PvpMaxDefense",
                "PvpDefense",
                "PveMinDefense",
                "PveMaxDefense",
                "PveDefense"
        );
    }


    private double getAttribute(double[] values, int type) {
        return values[type * 2] + SXAttribute.getRandom().nextDouble() * (values[type * 2 + 1] - values[type * 2]);
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        String[] loreSplit = lore.split("-");
        if (lore.contains(getString("PVEDefense.DiscernName"))) {
            values[4] += getNumber(loreSplit[0]);
            values[5] += getNumber(loreSplit[loreSplit.length > 1 ? 1 : 0]);

        } else if (lore.contains(getString("PVPDefense.DiscernName"))) {
            values[2] += getNumber(loreSplit[0]);
            values[3] += getNumber(loreSplit[loreSplit.length > 1 ? 1 : 0]);

        } else if (lore.contains(getString("Defense.DiscernName"))) {
            values[0] += getNumber(loreSplit[0]);
            values[1] += getNumber(loreSplit[loreSplit.length > 1 ? 1 : 0]);
        }
    }

    @Override
    public void correct(double[] values) {
        values[0] = Math.max(values[0], 0);
        values[2] = Math.max(values[2], 0);
        values[4] = Math.max(values[4], 0);
        values[1] = Math.max(values[0], values[1]);
        values[3] = Math.max(values[2], values[3]);
        values[5] = Math.max(values[4], values[5]);
    }

    @Override
    public double calculationCombatPower(double[] values) {
        double value = (values[0] + values[1]) / 2 * config().getInt("Defense.CombatPower");
        value += (values[2] + values[3]) / 2 * config().getInt("PVPDefense.CombatPower");
        value += (values[4] + values[5]) / 2 * config().getInt("PVEDefense.CombatPower");
        return value;
    }
}
