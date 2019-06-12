package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * 格挡
 *
 * @author Saukiya
 */
public class BlockAttribute extends SubAttribute {

    /**
     * double[0] 格挡几率
     * double[1] 格挡伤害
     */
    public BlockAttribute() {
        super("Block", 2, SXAttributeType.DEFENCE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            if (probability(getAttributes()[0])) {
                DamageEventData damageEventData = (DamageEventData) eventData;
                if (!(damageEventData.getEffectiveAttributeList().contains("Real") || damageEventData.getEffectiveAttributeList().contains("Reflection"))) {
                    damageEventData.getEffectiveAttributeList().add(this.getName());
                    double blockDamage = damageEventData.getDamage() * getAttributes()[1] / 100;
                    damageEventData.setDamage(damageEventData.getDamage() - blockDamage);
                    damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__BLOCK, getDf().format(blockDamage)));
                    Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__BLOCK, damageEventData.getEntityName(), getFirstPerson(), getDf().format(blockDamage));
                    Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__BLOCK, getFirstPerson(), damageEventData.getDamagerName(), getDf().format(blockDamage));
                }
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        if (string.equalsIgnoreCase("BlockRate")) {
            return getDf().format(getAttributes()[0]);
        } else if (string.equalsIgnoreCase("Block")) {
            return getDf().format(getAttributes()[1]);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("BlockRate", "Block");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_BLOCK_RATE))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        if (lore.contains(Config.getConfig().getString(Config.NAME_BLOCK))) {
            getAttributes()[1] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_BLOCK_RATE) + getAttributes()[1] * Config.getConfig().getInt(Config.VALUE_BLOCK);
    }
}
