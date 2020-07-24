package github.saukiya.sxattribute.data.attribute.sub.damage;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

/**
 * 凋零
 *
 * @author Saukiya
 */
public class WitherAttribute extends SubAttribute {

    /**
     * double[0] 凋零几率
     */
    public WitherAttribute() {
        super("Wither", 1, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof DamageEventData) {
            DamageEventData damageEventData = (DamageEventData) eventData;
            if (getAttributes()[0] > 0 && probability(getAttributes()[0] - damageEventData.getEntityAttributeDoubles("Toughness")[0])) {
                int tick = 40 + SXAttribute.getRandom().nextInt(60);
                damageEventData.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, tick, SXAttribute.getRandom().nextInt(2) + 1));
                damageEventData.sendHolo(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__WITHER, getDf().format(tick / 20D)));
                Message.send(damageEventData.getDamager(), Message.PLAYER__BATTLE__WITHER, damageEventData.getEntityName(), getFirstPerson(), getDf().format(tick / 20D));
                Message.send(damageEventData.getEntity(), Message.PLAYER__BATTLE__WITHER, getFirstPerson(), damageEventData.getDamagerName(), getDf().format(tick / 20D));
            }
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Wither") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Wither");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_WITHER))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_WITHER);
    }
}

