package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.Collections;
import java.util.List;

/**
 * 经验加成
 *
 * @author Saukiya
 */
public class ExpAddition extends SubAttribute implements Listener {

    /**
     * double[] 经验加成
     */
    public ExpAddition() {
        super(SXAttribute.getInst(), 1, AttributeType.OTHER);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message", "&7你的经验增加了 &6{0}&7! [&a+{1}%&7]");
        config.set("ExpAddition.DiscernName", "经验增幅");
        config.set("ExpAddition.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        // no event
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
        if (lore.contains(getString("ExpAddition.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], getConfig().getInt("ExpAddition.UpperLimit", Integer.MAX_VALUE));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("ExpAddition.CombatPower");
    }

    @EventHandler
    private void onExpChangeEvent(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        double expAddition = SXAttribute.getApi().getEntityData(player).getValues(getName())[0];
        if (event.getAmount() > 0 && expAddition > 0) {
            event.setAmount((int) (event.getAmount() * (100 + expAddition) / 100));
            send(player, "Message", event.getAmount(), expAddition);
        }
    }
}
