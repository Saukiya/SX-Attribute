package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import org.bukkit.EntityEffect;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

/**
 * 反射
 *
 * @author Saukiya
 */
public class Reflection extends SubAttribute {

    /**
     * double[0] 反射几率
     * double[1] 反射伤害
     */
    public Reflection() {
        super(SXAttribute.getInst(), 2, AttributeType.DEFENCE);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&6&o反伤: &b&o{0}");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 被 &c{1}&6 反弹伤害了!");
        config.set("ReflectionRate.DiscernName", "反射几率");
        config.set("ReflectionRate.UpperLimit", 80);
        config.set("ReflectionRate.CombatPower", 1);
        config.set("Reflection.DiscernName", "反射比例");
        config.set("Reflection.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            if (probability(values[0])) {
                DamageData damageData = (DamageData) eventData;
                if (!(damageData.getEffectiveAttributeList().contains("Real") || damageData.getEffectiveAttributeList().contains("Block"))) {
                    damageData.getEffectiveAttributeList().add(this.getName());
                    double damage = damageData.getDamage() * values[1] / 100;
                    LivingEntity damager = damageData.getAttacker();

                    damager.damage(damage, damageData.getDefender());
                    damager.playEffect(EntityEffect.HURT);
                    damageData.sendHolo(getString("Message.Holo", getDf().format(damage)));
                    send(damager, "Message.Battle", getFirstPerson(), damageData.getDefenderName(), getDf().format(damage));
                    send(damageData.getDefender(), "Message.Battle", damageData.getAttackerName(), getFirstPerson(), getDf().format(damage));
                }
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        switch (string) {
            case "ReflectionRate":
                return values[0];
            case "Reflection":
                return values[1];
        }
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "ReflectionRate",
                "Reflection"
        );
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("ReflectionRate.DiscernName"))) {
            values[0] += getNumber(lore);
        }
        if (lore.contains(getString("Reflection.DiscernName"))) {
            values[1] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], getConfig().getInt("ReflectionRate.UpperLimit", 100));
        values[1] = Math.min(values[0], getConfig().getInt("Reflection.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("ReflectionRate.CombatPower") +
                values[1] * getConfig().getInt("Reflection.CombatPower");
    }
}
