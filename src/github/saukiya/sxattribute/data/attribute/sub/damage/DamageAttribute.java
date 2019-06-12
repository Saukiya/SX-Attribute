package github.saukiya.sxattribute.data.attribute.sub.damage;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.spigotmc.SpigotConfig;

import java.util.Arrays;
import java.util.List;

/**
 * 攻击力
 *
 * @author Saukiya
 */
public class DamageAttribute extends SubAttribute {

    /**
     * double[0] 伤害最小值
     * double[1] 伤害最大值
     * double[2] 伤害最小值 - PVP
     * double[3] 伤害最大值 - PVP
     * double[4] 伤害最小值 - PVE
     * double[5] 伤害最大值 - PVE
     */
    public DamageAttribute() {
        super("Damage", 6, SXAttributeType.DAMAGE, SXAttributeType.UPDATE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            DamageEventData damageEventData = (DamageEventData) eventData;
            EntityDamageByEntityEvent event = damageEventData.getEvent();
            if (!Config.isDamageGauges() || event.getDamager() instanceof Projectile) {
                damageEventData.addDamage(getAttribute());
            } else if (event.getDamager() instanceof Player) {
                damageEventData.addDamage(getAttribute() - getAttributes()[0]);
            } else {
                damageEventData.addDamage(getAttribute());
            }
            if (event.getEntity() instanceof Player) {
                damageEventData.addDamage(getPVPAttribute());
            } else {
                damageEventData.addDamage(getPVEAttribute());
            }
        } else if (eventData instanceof UpdateEventData && ((UpdateEventData) eventData).getEntity() instanceof Player) {
            LivingEntity entity = ((UpdateEventData) eventData).getEntity();
            if (Config.isDamageGauges()) {
                entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(getAttributes()[0]);
            } else if (getAttribute() == 0D) {
                entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1);
            } else {
                entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.01);
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        if (string.equalsIgnoreCase("MinDamage")) {
            return getDf().format(getAttributes()[0]);
        } else if (string.equalsIgnoreCase("MaxDamage")) {
            return getDf().format(getAttributes()[1]);
        } else if (string.equalsIgnoreCase("Damage")) {
            return getAttributes()[0] == getAttributes()[1] ? getDf().format(getAttributes()[0]) : (getDf().format(getAttributes()[0]) + " - " + getDf().format(getAttributes()[1]));
        } else if (string.equalsIgnoreCase("PvpMinDamage")) {
            return getDf().format(getAttributes()[2]);
        } else if (string.equalsIgnoreCase("PvpMaxDamage")) {
            return getDf().format(getAttributes()[3]);
        } else if (string.equalsIgnoreCase("PvpDamage")) {
            return getAttributes()[2] == getAttributes()[3] ? getDf().format(getAttributes()[2]) : (getDf().format(getAttributes()[2]) + " - " + getDf().format(getAttributes()[3]));
        } else if (string.equalsIgnoreCase("PveMinDamage")) {
            return getDf().format(getAttributes()[4]);
        } else if (string.equalsIgnoreCase("PveMaxDamage")) {
            return getDf().format(getAttributes()[5]);
        } else if (string.equalsIgnoreCase("PveDamage")) {
            return getAttributes()[4] == getAttributes()[5] ? getDf().format(getAttributes()[4]) : (getDf().format(getAttributes()[4]) + " - " + getDf().format(getAttributes()[5]));
        } else {
            return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("MinDamage", "MaxDamage", "Damage", "PvpMinDamage", "PvpMaxDamage", "PvpDamage", "PveMinDamage", "PveMaxDamage", "PveDamage");
    }

    private double getAttribute() {
        return getAttributes()[0] + SXAttribute.getRandom().nextDouble() * (getAttributes()[1] - getAttributes()[0]);
    }

    private double getPVPAttribute() {
        return getAttributes()[2] + SXAttribute.getRandom().nextDouble() * (getAttributes()[3] - getAttributes()[2]);
    }

    private double getPVEAttribute() {
        return getAttributes()[4] + SXAttribute.getRandom().nextDouble() * (getAttributes()[5] - getAttributes()[4]);
    }

    @Override
    public boolean loadAttribute(String lore) {
        String[] loreSplit = lore.split("-");
        if (lore.contains(Config.getConfig().getString(Config.NAME_PVE_DAMAGE))) {
            this.getAttributes()[4] += Double.valueOf(getNumber(loreSplit[0]));
            this.getAttributes()[5] += Double.valueOf(getNumber(loreSplit.length > 1 ? loreSplit[1] : loreSplit[0]));
        } else if (lore.contains(Config.getConfig().getString(Config.NAME_PVP_DAMAGE))) {
            this.getAttributes()[2] += Double.valueOf(getNumber(loreSplit[0]));
            this.getAttributes()[3] += Double.valueOf(getNumber(loreSplit.length > 1 ? loreSplit[1] : loreSplit[0]));
        } else if (lore.contains(Config.getConfig().getString(Config.NAME_DAMAGE))) {
            this.getAttributes()[0] += Double.valueOf(getNumber(loreSplit[0]));
            this.getAttributes()[1] += Double.valueOf(getNumber(loreSplit.length > 1 ? loreSplit[1] : loreSplit[0]));
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void correct() {
        if (getAttributes()[0] <= 0) getAttributes()[0] = Config.isDamageGauges() ? 1 : 0;
        if (getAttributes()[0] > Double.MAX_VALUE) getAttributes()[0] = Double.MAX_VALUE;
        if (getAttributes()[0] > SpigotConfig.attackDamage) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cPlease set attackDamage to spigot.yml §8[§4" + getAttributes()[0] + "§7 > §4" + SpigotConfig.attackDamage + "§8]");
            getAttributes()[0] = SpigotConfig.attackDamage;
        }
        if (getAttributes()[1] < getAttributes()[0]) getAttributes()[1] = getAttributes()[0];
        if (getAttributes()[2] <= 0) getAttributes()[2] = 0;
        if (getAttributes()[3] < getAttributes()[2]) getAttributes()[3] = getAttributes()[2];
        if (getAttributes()[4] <= 0) getAttributes()[4] = 0;
        if (getAttributes()[5] < getAttributes()[4]) getAttributes()[5] = getAttributes()[4];
    }

    @Override
    public double getValue() {
        double value = (getAttributes()[0] + getAttributes()[1]) / 2 * Config.getConfig().getInt(Config.VALUE_DAMAGE);
        value += (getAttributes()[2] + getAttributes()[3]) / 2 * Config.getConfig().getInt(Config.VALUE_PVP_DAMAGE);
        value += (getAttributes()[4] + getAttributes()[5]) / 2 * Config.getConfig().getInt(Config.VALUE_PVE_DAMAGE);
        return value;
    }
}
