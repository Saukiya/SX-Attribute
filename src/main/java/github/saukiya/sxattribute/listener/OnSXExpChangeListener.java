package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxlevel.event.ChangeType;
import github.saukiya.sxlevel.event.SXExpChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Saukiya
 */
public class OnSXExpChangeListener implements Listener {

    private final SXAttribute plugin;

    public OnSXExpChangeListener(SXAttribute plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onExpChangeEvent(SXExpChangeEvent event) {
        if (event.isCancelled() || !event.getType().equals(ChangeType.ADD)) {
            return;
        }
        Player player = event.getPlayer();
        Double expAddition = plugin.getAttributeManager().getEntityData(player).getSubAttribute("ExpAddition").getAttributes()[0];
        if (event.getAmount() > 0 && expAddition > 0) {
            int exp = (int) (event.getAmount() * expAddition / 100);
            event.setAmount(exp + event.getAmount());
            Message.send(player, Message.getMsg(Message.PLAYER__EXP_ADDITION, event.getAmount(), expAddition));
        }
    }
}
