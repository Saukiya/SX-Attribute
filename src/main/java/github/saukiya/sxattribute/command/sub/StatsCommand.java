package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Saukiya
 */
public class StatsCommand extends SubCommand {

    public StatsCommand() {
        super(SXAttribute.getPlugin(), "stats", SenderType.PLAYER);
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        if (args.length > 1 && sender.hasPermission(plugin.getName() + ".admin")) {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player != null) {
                plugin.getStatsInventory().openStatsInventory(player, (Player) sender);
            } else {
                sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
            }
        }
        plugin.getStatsInventory().openStatsInventory((Player) sender);
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        return null;
    }
}
