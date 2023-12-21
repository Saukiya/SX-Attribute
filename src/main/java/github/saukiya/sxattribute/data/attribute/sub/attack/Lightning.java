package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.List;

/**
 * 雷霆
 *
 * @author Saukiya
 */
public class Lightning extends SubAttribute {

    /**
     * double[0] 雷霆几率
     */
    public Lightning() {
        super(SXAttribute.getInst(), 1, AttributeType.ATTACK);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&e&o雷霆: &b&o{0}");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 被 &c{1}&6 用雷电击中了!");
        config.set("Lightning.DiscernName", "雷霆几率");
        config.set("Lightning.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            if (values[0] > 0 && probability(values[0] - damageData.getDefenderData().getValues("Toughness")[0])) {
                damageData.getDefender().getWorld().strikeLightningEffect(damageData.getDefender().getLocation());
                double lightningDamage = damageData.getDefender().getHealth() * SXAttribute.getRandom().nextDouble() / 10;
                damageData.getDefender().setHealth(damageData.getDefender().getHealth() - lightningDamage);
                damageData.sendHolo(getString("Message.Holo", getDf().format(lightningDamage)));
                send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(lightningDamage));
                send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(lightningDamage));
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        return string.equals(getName()) ? values[0] : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList(getName());
    }


    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("Lightning.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], getConfig().getInt("Lightning.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("Lightning.CombatPower");
    }
}
