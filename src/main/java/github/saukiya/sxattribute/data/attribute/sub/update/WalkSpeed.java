package github.saukiya.sxattribute.data.attribute.sub.update;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 速度
 *
 * @author Saukiya
 */
public class WalkSpeed extends SubAttribute {

    /**
     * double[0] 移动速度
     */
    public WalkSpeed() {
        super(SXAttribute.getInst(), 1, AttributeType.UPDATE);
    }


    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("WalkSpeed.DiscernName", "移速增幅");
        config.set("WalkSpeed.Default", 0.2);
        config.set("WalkSpeed.UpperLimit", 400);
        config.set("WalkSpeed.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();
            player.setWalkSpeed((float) (getConfig().getDouble("WalkSpeed.Default") * (100 + values[0]) / 100D));
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        return string.equals(getName()) ? values[0] : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList(getName());
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("WalkSpeed.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        values[0] = Math.min(Math.max(values[0], -99), getConfig().getInt("WalkSpeed.UpperLimit", Integer.MAX_VALUE));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("WalkSpeed.CombatPower");
    }
}
