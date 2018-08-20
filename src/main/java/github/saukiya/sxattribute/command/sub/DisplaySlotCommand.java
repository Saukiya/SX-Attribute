package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Saukiya
 */
public class DisplaySlotCommand extends SubCommand {

    public DisplaySlotCommand() {
        super("displaySlot", SenderType.PLAYER);
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        if (plugin.getRegisterSlotManager().getRegisterSlotMap().size() > 0) {
            plugin.getDisplaySlotInventory().openDisplaySlotInventory((Player) sender);
        } else {
            sender.sendMessage(Message.getMsg(Message.PLAYER__NO_REGISTER_SLOTS));
        }
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        return null;
    }

}
