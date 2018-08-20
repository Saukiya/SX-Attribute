package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 * @author Saukiya
 * @since 2018年3月25日
 */

public class OnExpChangeListener implements Listener {

    private final SXAttribute plugin;

    public OnExpChangeListener(SXAttribute plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onExpChangeEvent(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        Double expAddition = plugin.getAttributeManager().getEntityData(player).getSubAttribute("ExpAddition").getAttributes()[0];
        if (event.getAmount() > 0 && expAddition > 0) {
            int exp = (int) (event.getAmount() * expAddition / 100);
            event.setAmount(exp + event.getAmount());
            Message.send(player, Message.getMsg(Message.PLAYER__EXP_ADDITION, event.getAmount(), expAddition));
        }
    }
}
