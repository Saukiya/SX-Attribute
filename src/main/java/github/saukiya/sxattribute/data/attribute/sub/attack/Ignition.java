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
 * 点燃
 *
 * @author Saukiya
 */
public class Ignition extends SubAttribute {

    /**
     * double[0] 点燃几率
     */
    public Ignition() {
        super(SXAttribute.getInst(), 1, AttributeType.ATTACK);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&c&o点燃: &b&o{0}s");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 被 &c{1}&6 点燃了!");
        config.set("Ignition.DiscernName", "点燃几率");
        config.set("Ignition.CombatPower", 1);
        config.set("Ignition.UpperLimit", 100);


        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            double toughness = effectiveOf("Toughness", damageData.getDefender(), damageData.getDefenderData().getValues("Toughness")[0]);
            // 触发几率公式 (变量 value=点燃几率, toughness=目标韧性); 默认 几率-韧性
            double chance = formula(damageData.getAttacker(), "Formula.Chance", values[0] - toughness, "value", values[0], "toughness", toughness);
            if (values[0] > 0 && probability(chance)) {
                // 点燃时长(tick)公式; 默认 40+随机0~59
                damageData.getDefender().setFireTicks((int) formula(damageData.getDefender(), "Formula.Ignition", 40 + SXAttribute.getRandom().nextInt(60)));
                if (isMessageEnabled()) damageData.sendHolo(getString("Message.Holo", getDf().format(damageData.getDefender().getFireTicks() / 20D)));
                send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(damageData.getDefender().getFireTicks() / 20D));
                send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(damageData.getDefender().getFireTicks() / 20D));
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
        if (lore.contains(getString("Ignition.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], config().getInt("Ignition.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("Ignition.CombatPower");
    }
}
