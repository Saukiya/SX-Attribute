package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.util.Config;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Saukiya
 */

public class ListenerBanShieldInteract implements Listener {

    @EventHandler
    void onPlayerClickEvent(PlayerInteractEvent event) {
        // 1.9.0 禁用盾牌右键
        if (!event.isCancelled()) {
            event.setCancelled(Config.isBanShieldDefense() && event.getItem() != null && event.getItem().getType().equals(Material.SHIELD));
        }
    }

}
