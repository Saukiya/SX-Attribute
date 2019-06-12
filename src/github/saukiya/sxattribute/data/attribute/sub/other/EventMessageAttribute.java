package github.saukiya.sxattribute.data.attribute.sub.other;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import github.saukiya.sxattribute.event.SXDamageEvent;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Saukiya
 */
public class EventMessageAttribute extends SubAttribute implements Listener {

    public EventMessageAttribute() {
        super("EventMessage", 0, SXAttributeType.OTHER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler
    void onSXDamageEvent(SXDamageEvent e) {
        DamageEventData damageEventData = e.getData();
        EntityDamageByEntityEvent event = damageEventData.getEvent();

        if (!event.isCancelled()) {
            damageEventData.sendHolo(Message.getMsg(damageEventData.isCrit() && event.getDamage() > 0 ? Message.PLAYER__HOLOGRAPHIC__CRIT : Message.PLAYER__HOLOGRAPHIC__DAMAGE, SXAttribute.getDf().format(event.getFinalDamage())));
        }

        if (Config.isHolographic() && SXAttribute.isHolographic() && !Config.getHolographicBlackList().contains(event.getCause().name())) {
            Location loc = damageEventData.getEntity().getEyeLocation().clone().add(0, 0.6 - SXAttribute.getRandom().nextDouble() / 2, 0);
            loc.setYaw(damageEventData.getDamager().getLocation().getYaw() + 90);
            loc.add(loc.getDirection().multiply(0.8D));
            Hologram hologram = HologramsAPI.createHologram(getPlugin(), loc);
            for (String message : damageEventData.getHoloList()) {
                hologram.appendTextLine(message);
            }
            SXAttribute.getApi().getHologramsList().add(hologram);
            new BukkitRunnable() {
                @Override
                public void run() {
                    hologram.delete();
                    SXAttribute.getApi().getHologramsList().remove(hologram);
                }
            }.runTaskLater(getPlugin(), Config.getConfig().getInt(Config.HOLOGRAPHIC_DISPLAY_TIME));
        }
    }

    @Override
    public void eventMethod(EventData eventData) {

    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return null;
    }

    @Override
    public boolean loadAttribute(String lore) {
        return false;
    }

    @Override
    public double getValue() {
        return 0;
    }
}
