package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxlevel.event.ChangeType;
import github.saukiya.sxlevel.event.SXExpChangeEvent;
import org.bukkit.Bukkit;
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
public class ExpAdditionAttribute extends SubAttribute {

    /**
     * double[] 经验加成
     */
    public ExpAdditionAttribute() {
        super("ExpAddition", 1, SXAttributeType.OTHER);
    }

    @Override
    public void onEnable() {
        // 属性启动时注册监听器
        Bukkit.getPluginManager().registerEvents(SXAttribute.isSxLevel() ? new OnSXExpChangeListener() : new OnExpChangeListener(), getPlugin());
    }

    @Override
    public void eventMethod(EventData eventData) {
        // no event
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("ExpAddition") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("ExpAddition");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_EXP_ADDITION))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_EXP_ADDITION);
    }

    /**
     * 默认经验监听器
     */
    private class OnExpChangeListener implements Listener {
        @EventHandler
        private void onExpChangeEvent(PlayerExpChangeEvent event) {
            Player player = event.getPlayer();
            Double expAddition = SXAttribute.getApi().getEntityAllData(player).getSubAttribute("ExpAddition").getAttributes()[0];
            if (event.getAmount() > 0 && expAddition > 0) {
                int exp = (int) (event.getAmount() * expAddition / 100);
                event.setAmount(exp + event.getAmount());
                Message.send(player, Message.getMsg(Message.PLAYER__EXP_ADDITION, event.getAmount(), expAddition));
            }
        }
    }

    /**
     * SX-level 经验监听器
     */
    private class OnSXExpChangeListener implements Listener {
        @EventHandler
        private void onExpChangeEvent(SXExpChangeEvent event) {
            if (event.isCancelled() || !event.getType().equals(ChangeType.ADD)) {
                return;
            }
            Player player = event.getPlayer();
            Double expAddition = SXAttribute.getApi().getEntityAllData(player).getSubAttribute("ExpAddition").getAttributes()[0];
            if (event.getAmount() > 0 && expAddition > 0) {
                int exp = (int) (event.getAmount() * expAddition / 100);
                event.setAmount(exp + event.getAmount());
                Message.send(player, Message.getMsg(Message.PLAYER__EXP_ADDITION, event.getAmount(), expAddition));
            }
        }
    }
}
