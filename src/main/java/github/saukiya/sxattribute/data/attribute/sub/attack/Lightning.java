package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
            double toughness = effectiveOf("Toughness", damageData.getDefender(), damageData.getDefenderData().getValues("Toughness")[0]);
            // 触发几率公式 (变量 value=雷霆几率, toughness=目标韧性); 默认 几率-韧性
            double chance = formula(damageData.getAttacker(), "Formula.Chance", values[0] - toughness, "value", values[0], "toughness", toughness);
            if (values[0] > 0 && probability(chance)) {
                damageData.getDefender().getWorld().strikeLightningEffect(damageData.getDefender().getLocation());
                double health = damageData.getDefender().getHealth();
                // 雷霆伤害公式 (变量 health=目标当前生命); 默认 生命*随机0~1/10
                double lightningDamage = formula(damageData.getDefender(), "Formula.Lightning", health * SXAttribute.getRandom().nextDouble() / 10, "health", health);
                damageData.getDefender().setHealth(Math.max(0, health - lightningDamage));
                if (isMessageEnabled()) damageData.sendHolo(getString("Message.Holo", getDf().format(lightningDamage)));
                send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(lightningDamage));
                send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(lightningDamage));
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
        if (lore.contains(getString("Lightning.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], config().getInt("Lightning.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("Lightning.CombatPower");
    }
}
