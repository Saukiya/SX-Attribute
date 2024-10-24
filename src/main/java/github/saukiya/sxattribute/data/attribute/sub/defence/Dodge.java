package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 闪避
 *
 * @author Saukiya
 */
public class Dodge extends SubAttribute {

    /**
     * double[0] 闪避几率
     */
    public Dodge() {
        super(SXAttribute.getInst(), 1, AttributeType.DEFENCE);
    }


    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&a&o闪避");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 躲开了 &c{1}&6 的攻击!");
        config.set("Dodge.DiscernName", "格挡几率");
        config.set("Dodge.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            if (values[0] > 0 && probability(values[0] - damageData.getAttackerData().getValues("HitRate")[0])) {
                damageData.setCancelled(true);
                Location loc = damageData.getAttacker().getLocation().clone();
                loc.setYaw(loc.getYaw() + SXAttribute.getRandom().nextInt(80) - 40);
                damageData.getDefender().setVelocity(loc.getDirection().setY(0.1).multiply(0.7));
                damageData.sendHolo(getString("Message.Holo"));
                send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson());
                send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName());
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        return string.equals(getName()) ? values[0] : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList(getName());
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("Dodge.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], config().getInt("Dodge.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("Dodge.CombatPower");
    }
}
