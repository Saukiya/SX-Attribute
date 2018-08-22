package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 */
public class DamageEventData extends EventData {

    @Getter
    private final LivingEntity entity;

    @Getter
    private final LivingEntity damager;

    @Getter
    private final String entityName;

    @Getter
    private final String damagerName;

    @Getter
    private final SXAttributeData entityData;

    @Getter
    private final SXAttributeData damagerData;

    @Getter
    private final EntityDamageByEntityEvent event;

    @Getter
    private final List<String> EffectiveAttributeList = new ArrayList<>();

    @Getter
    private final List<String> holoList = new ArrayList<>();

    @Getter
    @Setter
    private double damage;

    @Getter
    @Setter
    private boolean crit;

    @Getter
    @Setter
    private boolean cancelled = false;

    public DamageEventData(LivingEntity entity, LivingEntity damager, String entityName, String damagerName, SXAttributeData entityData, SXAttributeData damagerData, EntityDamageByEntityEvent event) {
        this.entity = entity;
        this.damager = damager;
        this.entityName = entityName;
        this.damagerName = damagerName;
        this.entityData = entityData;
        this.damagerData = damagerData;
        this.event = event;
        this.damage = event.getDamage();
    }

    public void sendHolo(String message) {
        if (!message.contains("Null Message: ")) {
            holoList.add(message);
        }
    }

    public Double[] getEntityAttributeDoubles(String attributeName) {
        SubAttribute attribute = entityData.getSubAttribute(attributeName);
        return attribute != null ? attribute.getAttributes() : new Double[]{0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D};
    }

    public SubAttribute getDamageAttribute(String attributeName) {
        return damagerData.getSubAttribute(attributeName);
    }

    public void addDamage(double addDamage) {
        damage += addDamage;
    }

    public void takeDamage(double takeDamage) {
        damage -= takeDamage;
    }

}
