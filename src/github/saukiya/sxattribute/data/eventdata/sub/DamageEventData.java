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
 * 伤害事件统计
 *
 * @author Saukiya
 */
public class DamageEventData extends EventData {

    private final LivingEntity entity;

    private final LivingEntity damager;

    private final String entityName;

    private final String damagerName;

    private final SXAttributeData entityData;

    private final SXAttributeData damagerData;

    private final EntityDamageByEntityEvent event;

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
     * 获取防御方的属性值
     *
     * @param attributeName String
     * @return Double[]
     */
    public double[] getEntityAttributeDoubles(String attributeName) {
        SubAttribute attribute = entityData.getSubAttribute(attributeName);
        return attribute != null ? attribute.getAttributes() : new double[12];
    }

    /**
     * 获取攻击方的属性值
     *
     * @param attributeName String
     * @return Double[]
     */
    public double[] getDamagerAttributeDoubles(String attributeName) {
        SubAttribute attribute = damagerData.getSubAttribute(attributeName);
        return attribute != null ? attribute.getAttributes() : new double[12];
    }

    /**
     * 增加伤害值
     *
     * @param addDamage double
     */
    public void addDamage(double addDamage) {
        damage += addDamage;
    }

    /**
     * 减少伤害值
     *
     * @param takeDamage double
     */
    public void takeDamage(double takeDamage) {
        damage -= takeDamage;
    }

    /**
     * 获取防御方
     *
     * @return LivingEntity
     */
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * 获取攻击方
     *
     * @return LivingEntity
     */
    public LivingEntity getDamager() {
        return damager;
    }

    /**
     * 获取防御方名字
     *
     * @return String
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * 获取攻击方名字
     *
     * @return String
     */
    public String getDamagerName() {
        return damagerName;
    }

    /**
     * 获取原事件
     * 注意，事件内的FinalDamage是无效的
     *
     * @return EntityDamageByEntityEvent
     */
    public EntityDamageByEntityEvent getEvent() {
        return event;
    }

    /**
     * 获取被触发的效果
     *
     * @return List
     */
    public List<String> getEffectiveAttributeList() {
        return EffectiveAttributeList;
    }

    /**
     * 取消该事件 剩下的属性不会执行方法
     *
     * @param cancelled boolean
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
