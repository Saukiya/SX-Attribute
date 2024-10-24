package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.Arrays;
import java.util.List;

/**
 * 吸血
 *
 * @author Saukiya
 */
public class LifeSteal extends SubAttribute {

    /**
     * double[0] 吸血几率
     * double[1] 吸血倍率
     */
    public LifeSteal() {
        super(SXAttribute.getInst(), 2, AttributeType.ATTACK);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&c&o吸取: &b&o{0}");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 被 &c{1}&6 偷取生命了!");
        config.set("LifeStealRate.DiscernName", "吸血几率");
        config.set("LifeStealRate.CombatPower", 1);
        config.set("LifeSteal.DiscernName", "吸血倍率");
        config.set("LifeSteal.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            if (probability(values[0])) {
                DamageData damageData = (DamageData) eventData;
                LivingEntity damager = damageData.getAttacker();
                double maxHealth = SXAttribute.getApi().getMaxHealth(damager);
                double lifeHealth = damageData.getDamage() * values[1] / 100;
                EntityRegainHealthEvent event = new EntityRegainHealthEvent(damager, lifeHealth, EntityRegainHealthEvent.RegainReason.CUSTOM);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                lifeHealth = (maxHealth < damager.getHealth() + event.getAmount()) ? (maxHealth - damager.getHealth()) : event.getAmount();
                damager.setHealth(damager.getHealth() + lifeHealth);
                damageData.sendHolo(getString("Message.Holo", getDf().format(lifeHealth)));
                send(damager, "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(lifeHealth));
                send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(lifeHealth));
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        switch (string) {
            case "LifeStealRate":
                return values[0];
            case "LifeSteal":
                return values[1];
            default:
                return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "LifeStealRate",
                "LifeSteal"
        );
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("LifeStealRate.DiscernName"))) {
            values[0] += getNumber(lore);
        }
        if (lore.contains(getString("LifeSteal.DiscernName"))) {
            values[1] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], config().getInt("LifeStealRate.UpperLimit", 100));
        values[1] = Math.min(values[0], config().getInt("LifeSteal.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("LifeStealRate.CombatPower") +
                values[1] * config().getInt("LifeSteal.CombatPower");
    }
}
