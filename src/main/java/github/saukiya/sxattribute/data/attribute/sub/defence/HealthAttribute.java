package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateEventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.spigotmc.SpigotConfig;

import java.util.Collections;
import java.util.List;

/**
 * 生命 - 当前不更新怪物生命
 *
 * @author Saukiya
 */
public class HealthAttribute extends SubAttribute {

    /**
     * 生命
     * double[0] 生命值
     */
    public HealthAttribute() {
        super("Health", 1, SXAttributeType.UPDATE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof UpdateEventData && ((UpdateEventData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateEventData) eventData).getEntity();
            if (player.getHealth() > getAttributes()[0]) player.setHealth(getAttributes()[0]);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(getAttributes()[0]);
            int healthScale = Config.getConfig().getInt(Config.HEALTH_SCALED_VALUE);
            if (Config.isHealthScaled() && healthScale < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                player.setHealthScaled(true);
                player.setHealthScale(Config.getConfig().getInt(Config.HEALTH_SCALED_VALUE));
            } else {
                player.setHealthScaled(false);
            }
        }
    }

    @Override
    public String getPlaceholder(String string) {
        return string.equalsIgnoreCase("MaxHealth") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("MaxHealth");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_HEALTH))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public void correct() {
        if (getAttributes()[0] < 0) getAttributes()[0] = 1D;
        if (getAttributes()[0] > Double.MAX_VALUE) getAttributes()[0] = Double.MAX_VALUE;
        if (getAttributes()[0] > SpigotConfig.maxHealth){
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cPlease set maxHealth to spigot.yml §8[§4" + getAttributes()[0] + "§7 > §4"+SpigotConfig.maxHealth + "§8]");
            getAttributes()[0] = SpigotConfig.maxHealth;
        }
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_HEALTH);
    }
}
