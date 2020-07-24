package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 生命恢复
 *
 * @author Saukiya
 */
public class HealthRegenAttribute extends SubAttribute {

    /**
     * double[0] 生命回复
     */
    public HealthRegenAttribute() {
        super("HealthRegen", 1, SXAttributeType.OTHER);
    }

    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
                        if (player != null && !player.isDead() && player.isOnline()) {
                            Double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                            if (player.getHealth() < maxHealth) {
                                Double healthRegen = SXAttribute.getApi().getEntityAllData(player).getSubAttribute("HealthRegen").getAttributes()[0];
                                if (healthRegen > 0) {
                                    EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, healthRegen, EntityRegainHealthEvent.RegainReason.CUSTOM);
                                    Bukkit.getPluginManager().callEvent(event);
                                    if (!event.isCancelled()) {
                                        healthRegen = (event.getAmount() + player.getHealth()) > maxHealth ? (maxHealth - player.getHealth()) : event.getAmount();
                                        player.setHealth(healthRegen + player.getHealth());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§4生命恢复系统崩溃 正在重新启动!");
                    this.cancel();
                    HealthRegenAttribute.this.onEnable();
                    Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c启动完毕!");
                    Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c如果此消息连续刷屏，请通过Yum重载本插件");
                }
            }
        }.runTaskTimer(getPlugin(), 19, 20);
    }

    @Override
    public void eventMethod(EventData eventData) {
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("HealthRegen") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("HealthRegen");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_HEALTH_REGEN))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_HEALTH_REGEN);
    }
}
