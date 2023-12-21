package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.List;

/**
 * 命中
 *
 * @author Saukiya
 */
public class HitRate extends SubAttribute {

    /**
     * double[0] 命中几率
     */
    public HitRate() {
        super(SXAttribute.getInst(), 1, AttributeType.OTHER);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("HitRate.DiscernName", "命中几率");
        config.set("HitRate.CombatPower", 1);
        config.set("HitRate.UpperLimit", 80);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
    }


    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        return string.equals(getName()) ? values[0] : null;
    }


    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList(getName());
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("HitRate.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], getConfig().getInt("HitRate.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("HitRate.CombatPower");
    }
}
