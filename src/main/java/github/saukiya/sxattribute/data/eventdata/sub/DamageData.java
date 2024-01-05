package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.eventdata.EventData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 伤害事件统计
 *
 * @author Saukiya
 */
@Getter
public class DamageData implements EventData {

    private final LivingEntity defender;

    private final LivingEntity attacker;

    private final String defenderName;

    private final String attackerName;

    private final SXAttributeData defenderData;

    @Setter
    private SXAttributeData attackerData;

    private final EntityDamageByEntityEvent event;

    private final List<String> effectiveAttributeList = new ArrayList<>();

    private final List<String> holoList = new ArrayList<>();

    private final HashMap<String, Double> damages = new HashMap<>();

    @Setter
    private boolean crit;

    private boolean cancelled = false;

    @Setter
    private boolean fromAPI = false;

    public DamageData(LivingEntity defender, LivingEntity attacker, String defenderName, String attackerName, SXAttributeData defenderData, SXAttributeData attackerData, EntityDamageByEntityEvent event) {
        this.defender = defender;
        this.attacker = attacker;
        this.defenderName = defenderName;
        this.attackerName = attackerName;
        this.defenderData = defenderData;
        this.attackerData = attackerData;
        this.event = event;
        setDamage(event.getDamage());
    }

    /**
     * 输出一个全息文本
     *
     * @param message String
     */
    public void sendHolo(String message) {
        if (!message.contains("Null Message: ")) {
            holoList.add(message);
        }
    }

    public double getDamage() {
        AtomicReference<Double> all = new AtomicReference<>(0D);
//        Double reduce = damages.values().stream().filter(d -> d >= 0).reduce(0D, Double::sum);
//        System.out.println("getDamage: " + all + ", " + reduce);
//        System.out.println(damages);
//        if (reduce + all.get() < 0) {
//            return 0;
//        }
        damages.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("All") && v > 0) {
                all.updateAndGet(v1 -> v1 + v);
            }
        });
        all.updateAndGet(v1 -> v1 + damages.getOrDefault("All", 0.0));
        System.out.println(damages);
        return all.get() < 0.0 ? 0.0 : all.get();
    }

    /**
     * 设置伤害值
     *
     * @param damage double
     */
    public void setDamage(double damage) {
        setDamage(damage, "Default");
    }

    public void setDamage(double damage, String type) {
        damages.put(type, damage);
        event.setDamage(getDamage());
    }

    /**
     * 增加伤害值
     *
     * @param addDamage double
     */
    public void addDamage(double addDamage) {
        addDamage(addDamage, "Default");
    }

    public void addDamage(double addDamage, String type) {
        damages.put(type, damages.getOrDefault(type, 0D) + addDamage);
        event.setDamage(getDamage());
    }

    /**
     * 减少伤害值
     *
     * @param takeDamage double
     */
    public void takeDamage(double takeDamage) {
        takeDamage(takeDamage, "Default");
    }

    public void takeDamage(double takeDamage, String type) {
        damages.put(type, damages.getOrDefault(type, 0D) - takeDamage);
        event.setDamage(getDamage());
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        event.setCancelled(cancelled);
    }
}
