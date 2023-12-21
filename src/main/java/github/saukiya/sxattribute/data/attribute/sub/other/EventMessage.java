package github.saukiya.sxattribute.data.attribute.sub.other;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.sxattribute.event.SXDamageEvent;
import github.saukiya.sxattribute.util.Config;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 */
public class EventMessage extends SubAttribute implements Listener {

    @Getter
    private List<HoloData> holoList = null;

    public EventMessage() {
        super(SXAttribute.getInst(), 0, AttributeType.OTHER);
    }

    @Override
    public void onEnable() {
        if (SXAttribute.isHolographic()) {
            holoList = new ArrayList<>();
            new BukkitRunnable() {
                @Override
                public void run() {
                    double moveDistance = 0.1D / Config.getConfig().getInt(Config.HOLOGRAPHIC_DISPLAY_TIME);
                    for (int i = holoList.size() - 1; i >= 0; i--) {
                        HoloData holoData = holoList.get(i);
                        if (holoData.getClearTime() < System.currentTimeMillis()) {
                            holoData.delete();
                        } else {
                            holoData.getHologram().teleport(holoData.getHologram().getLocation().add(0, moveDistance, 0));
                        }
                    }
                }
            }.runTaskTimer(getPlugin(), 20, 2);
        }
    }

    @Override
    public void onDisable() {
        if (SXAttribute.isHolographic()) {
            for (HoloData data : getHoloList()) {
                data.getHologram().delete();
            }
        }
    }

    @EventHandler
    public void onSXDamageEvent(SXDamageEvent event) {
        DamageData damageData = event.getData();
        if (Config.isHolographic() && SXAttribute.isHolographic()) {
            Location loc = damageData.getDefender().getEyeLocation().clone().add(0, 0.6 - SXAttribute.getRandom().nextDouble() / 2, 0);
            loc.setYaw(damageData.getAttacker().getLocation().getYaw() + 90);
            loc.add(loc.getDirection().multiply(0.8D));
            new HoloData(loc, damageData.getHoloList());
        }

    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
    }

    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return null;
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return 0;
    }

    public class HoloData {

        @Getter
        private long clearTime = System.currentTimeMillis() + (Config.getConfig().getInt(Config.HOLOGRAPHIC_DISPLAY_TIME) * 1000);

        @Getter
        private Hologram hologram;

        public HoloData(Location loc, List<String> list) {
            this.hologram = HologramsAPI.createHologram(getPlugin(), loc);
            for (String message : list) {
                hologram.appendTextLine(message);
            }
            EventMessage.this.getHoloList().add(this);
        }

        public void delete() {
            hologram.delete();
            EventMessage.this.getHoloList().remove(this);
        }
    }

}
