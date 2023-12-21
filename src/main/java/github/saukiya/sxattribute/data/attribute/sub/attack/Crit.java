package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.sxattribute.event.SXDamageEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

/**
 * 暴击
 * <p>
 * double[0] 暴击几率
 * double[1] 暴击伤害
 *
 * @author Saukiya
 */
public class Crit extends SubAttribute implements Listener {

    public Crit() {
        super(SXAttribute.getInst(), 2, AttributeType.ATTACK);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&a&o暴击: &b&o+{0}");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 对 &c{1}&6 造成了暴击! &8[&c{2}&8]");
        config.set("Crit.DiscernName", "暴伤增幅");
        config.set("Crit.CombatPower", 1);
        config.set("Crit.UpperLimit", 1000);
        config.set("CritRate.DiscernName", "暴击几率");
        config.set("CritRate.CombatPower", 1);
        return config;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSXDamageEvent(SXDamageEvent event) {
        DamageData damageData = event.getData();
        if (!damageData.isCancelled() && damageData.isCrit()) {
            String damage = getDf().format(damageData.getEvent().getFinalDamage());
            damageData.sendHolo(getString("Message.Holo", damage));
            send(damageData.getAttacker(), "Message.Battle", getFirstPerson(), damageData.getDefenderName(), damage);
            send(damageData.getDefender(), "Message.Battle", damageData.getAttackerName(), getFirstPerson(), damage);
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            if (probability(values[0])) {
                DamageData damageData = (DamageData) eventData;
                damageData.setCrit(true);
                damageData.setDamage(damageData.getDamage() * (100 + values[1]) / 100);
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        switch (string) {
            case "CritRate":
                return values[0];
            case "Crit":
                return values[1];
            case "Crit_100":
                return values[1] + 100;
            default:
                return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "CritRate",
                "Crit",
                "Crit_100"
        );
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("CritRate.DiscernName"))) {
            values[0] += getNumber(lore);
        } else if (lore.contains(getConfig().getString("Crit.DiscernName"))) {
            values[1] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], getConfig().getInt("CritRate.UpperLimit", 100));
        values[1] = Math.min(values[1], getConfig().getInt("Crit.UpperLimit", 1000));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("CritRate.CombatPower") +
                values[1] * getConfig().getInt("Crit.CombatPower");
    }
}
