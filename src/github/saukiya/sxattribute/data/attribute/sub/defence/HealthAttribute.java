package github.saukiya.sxattribute.data.attribute.sub.defence;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.manager.AttributeManager;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateEventData;
import github.saukiya.sxattribute.listener.OnHealthChangeDisplayListener;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * 生命 - 当前不更新怪物生命
 *
 * @author Saukiya
 */
public class HealthAttribute extends SubAttribute {

    private static boolean skillAPI = false;

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
            if (skillAPI) {
                SkillAPI.getPlayerData(player).getAttribute(AttributeManager.HEALTH);
            }
            double maxHealth = getAttributes()[0] + getSkillAPIHealth(player);
            if (player.getHealth() > maxHealth) player.setHealth(maxHealth);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            int healthScale = Config.getConfig().getInt(Config.HEALTH_SCALED_VALUE);
            if (Config.isHealthScaled() && healthScale < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                player.setHealthScaled(true);
                player.setHealthScale(Config.getConfig().getInt(Config.HEALTH_SCALED_VALUE));
            } else {
                player.setHealthScaled(false);
            }
        }
    }

    private int getSkillAPIHealth(Player player) {
        return skillAPI ? SkillAPI.getPlayerData(player).getClasses().stream().mapToInt(aClass -> (int) aClass.getHealth()).sum() : 0;
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("SkillAPI") != null) {
            skillAPI = true;
        }
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("MaxHealth") ? getDf().format(OnHealthChangeDisplayListener.getMaxHealth(player))
                : string.equalsIgnoreCase("Health") ? getDf().format(player.getHealth()) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("MaxHealth", "Health");
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
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_HEALTH);
    }
}
