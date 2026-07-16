package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import org.bukkit.EntityEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import github.saukiya.sxattribute.util.FoliaScheduler;

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
            double toughness = effectiveOf("Toughness", damageData.getDefender(), damageData.getDefenderData().getValues("Toughness")[0]);
            // 触发几率公式 (变量 value=撕裂几率, toughness=目标韧性); 默认 几率-韧性
            double chance = formula(damageData.getAttacker(), "Formula.Chance", values[0] - toughness, "value", values[0], "toughness", toughness);
            if (values[0] > 0 && probability(chance)) {
                int size = SXAttribute.getRandom().nextInt(3) + 1;
                // 每跳撕裂伤害公式 (变量 health=目标当前生命); 默认 生命/100
                double tearingDamage = formula(damageData.getDefender(), "Formula.Tearing", damageData.getDefender().getHealth() / 100, "health", damageData.getDefender().getHealth());
                FoliaScheduler.runEntityTimer(damageData.getDefender(), getPlugin(), new BukkitRunnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        i++;
                        if (i >= 12 / size || damageData.getDefender().isDead() || damageData.getEvent().isCancelled())
                            cancel();
                        damageData.getDefender().playEffect(EntityEffect.HURT);
                        // 这里没有发布合成事件，旧构造器已被新 API 标记移除；直接使用公式伤害可保持跨版本行为一致。
                        if (!damageData.getEvent().isCancelled()) {
                            double damage = Math.min(damageData.getDefender().getHealth(), tearingDamage);
                            damageData.getDefender().setHealth(damageData.getDefender().getHealth() - damage);
                            if (SXAttribute.isHigherVersion()) {
                                damageData.getDefender().getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, damageData.getDefender().getEyeLocation().add(0, -1, 0), 2, 0.2D, 0.2D, 0.2D, 0.1f);
                            }
                            if (damageData.getAttacker() instanceof Player) {
                                String soundName = "ENTITY_" + damageData.getDefender().getType() + "_HURT";
                                for (Sound value : Sound.values()) {
                                    if (value.name().equals(soundName)) {
                                        ((Player) damageData.getAttacker()).playSound(damageData.getDefender().getEyeLocation(), value, 1, 1);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                }, 5, size);
                if (isMessageEnabled()) damageData.sendHolo(getString("Message.Holo", getDf().format(tearingDamage * 12 / size)));
                send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(tearingDamage * 12 / size));
                send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(tearingDamage * 12 / size));
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
        if (lore.contains(getString("Tearing.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], config().getInt("Tearing.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("Tearing.CombatPower");
    }
}
