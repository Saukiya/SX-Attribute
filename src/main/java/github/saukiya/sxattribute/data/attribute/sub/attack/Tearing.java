package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.util.nms.NMS;
import org.bukkit.EntityEffect;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

/**
 * 撕裂
 *
 * @author Saukiya
 */
public class Tearing extends SubAttribute {

    /**
     * double[0] 撕裂几率
     */
    public Tearing() {
        super(SXAttribute.getInst(), 1, AttributeType.ATTACK);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&c&o撕裂: &b{0}");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 被 &c{1}&6 撕裂了!");
        config.set("Tearing.DiscernName", "撕裂几率");
        config.set("Tearing.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            if (values[0] > 0 && probability(values[0] - damageData.getDefenderData().getValues("Toughness")[0])) {
                int size = SXAttribute.getRandom().nextInt(3) + 1;
                double tearingDamage = damageData.getDefender().getHealth() / 100;
                new BukkitRunnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        i++;
                        if (i >= 12 / size || damageData.getDefender().isDead() || damageData.getEvent().isCancelled())
                            cancel();
                        damageData.getDefender().playEffect(EntityEffect.HURT);
                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damageData.getAttacker(), damageData.getDefender(), EntityDamageEvent.DamageCause.CUSTOM, tearingDamage);
                        if (!event.isCancelled()) {
                            double damage = damageData.getDefender().getHealth() < event.getDamage() ? damageData.getDefender().getHealth() : event.getDamage();
                            damageData.getDefender().setHealth(damageData.getDefender().getHealth() - damage);
                            if (NMS.compareTo(1,9,0) >= 0) {
                                damageData.getDefender().getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, damageData.getDefender().getEyeLocation().add(0, -1, 0), 2, 0.2D, 0.2D, 0.2D, 0.1f);
                            }
                            if (damageData.getAttacker() instanceof Player) {
                                ((Player) damageData.getAttacker()).playSound(damageData.getDefender().getEyeLocation(), "ENTITY_" + damageData.getDefender().getType().toString() + "_HURT", 1, 1);
                            }
                        }
                    }
                }.runTaskTimer(getPlugin(), 5, size);
                damageData.sendHolo(getString("Message.Holo", getDf().format(tearingDamage * 12 / size)));
                send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(tearingDamage * 12 / size));
                send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(tearingDamage * 12 / size));
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
        if (lore.contains(getString("Tearing.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], getConfig().getInt("Tearing.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("Tearing.CombatPower");
    }
}
