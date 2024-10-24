package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.eventdata.EventData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 伤害事件统计
 *
 * @author Saukiya
 */
public class DamageData implements EventData {

    @Getter
    private final LivingEntity defender;

    @Getter
    private final LivingEntity attacker;

    @Getter
    private final String defenderName;

    @Getter
    private final String attackerName;

    @Getter
    private final SXAttributeData defenderData;

    @Getter
    private final SXAttributeData attackerData;

    @Getter
    private final EntityDamageByEntityEvent event;

    @Getter
    private final List<String> effectiveAttributeList = new ArrayList<>();

    @Getter
    private final List<String> holoList = new ArrayList<>();

    @Getter
    private double damage;

    @Getter
    @Setter
    private boolean crit;

    @Getter
    private boolean cancelled = false;

    public DamageData(LivingEntity defender, LivingEntity attacker, String defenderName, String attackerName, SXAttributeData defenderData, SXAttributeData attackerData, EntityDamageByEntityEvent event) {
        this.defender = defender;
        this.attacker = attacker;
        this.defenderName = defenderName;
        this.attackerName = attackerName;
        this.defenderData = defenderData;
        this.attackerData = attackerData;
        this.event = event;
        this.damage = event.getDamage();
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

    /**
     * 设置伤害值
     *
     * @param addDamage double
     */
    public void setDamage(double addDamage) {
        damage = addDamage;
        event.setDamage(getDamage());
    }

    /**
     * 增加伤害值
     *
     * @param addDamage double
     */
    public void addDamage(double addDamage) {
        damage += addDamage;
        event.setDamage(getDamage());
    }

    /**
     * 减少伤害值
     *
     * @param takeDamage double
     */
    public void takeDamage(double takeDamage) {
        damage -= takeDamage;
        event.setDamage(getDamage());
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        event.setCancelled(cancelled);
    }
}
