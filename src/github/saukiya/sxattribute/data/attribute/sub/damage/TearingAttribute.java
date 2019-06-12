package github.saukiya.sxattribute.data.attribute.sub.damage;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.EntityEffect;
import org.bukkit.Particle;
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
public class TearingAttribute extends SubAttribute {

    /**
     * double[0] 撕裂几率
     */
    public TearingAttribute() {
        super("Tearing", 1, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            DamageEventData damageEventData = (DamageEventData) eventData;
            if (getAttributes()[0] > 0 && probability(getAttributes()[0] - damageEventData.getEntityAttributeDoubles("Toughness")[0])) {
                int size = SXAttribute.getRandom().nextInt(3) + 1;
                double tearingDamage = damageEventData.getEntity().getHealth() / 100;
                new BukkitRunnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        i++;
                        if (i >= 12 / size || damageEventData.getEntity().isDead() || damageEventData.getEvent().isCancelled())
                            cancel();
                        damageEventData.getEntity().playEffect(EntityEffect.HURT);
                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damageEventData.getDamager(), damageEventData.getEntity(), EntityDamageEvent.DamageCause.CUSTOM, tearingDamage);
                        if (!event.isCancelled()) {
                            double damage = damageEventData.getEntity().getHealth() < event.getDamage() ? damageEventData.getEntity().getHealth() : event.getDamage();
                            damageEventData.getEntity().setHealth(damageEventData.getEntity().getHealth() - damage);
                            if (SXAttribute.getVersionSplit()[1] >= 9) {
                                damageEventData.getEntity().getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, damageEventData.getEntity().getEyeLocation().add(0, -1, 0), 2, 0.2D, 0.2D, 0.2D, 0.1f);
                            }
                            if (damageEventData.getDamager() instanceof Player) {
                                ((Player) damageEventData.getDamager()).playSound(damageEventData.getEntity().getEyeLocation(), "ENTITY_" + damageEventData.getEntity().getType().toString() + "_HURT", 1, 1);
                            }
                        }
                    }
                }.runTaskTimer(getPlugin(), 5, size);
                damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__TEARING, getDf().format(tearingDamage * 12 / size)));
                Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__TEARING, damageEventData.getEntityName(), getFirstPerson(), getDf().format(tearingDamage * 12 / size));
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__TEARING, getFirstPerson(), damageEventData.getDamagerName(), getDf().format(tearingDamage * 12 / size));
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Tearing") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Tearing");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_TEARING))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_TEARING);
    }
}
