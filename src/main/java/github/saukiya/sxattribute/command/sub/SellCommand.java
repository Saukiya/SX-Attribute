package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.inventory.SellInventory;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * @author Saukiya
 */
public class SellCommand extends SubCommand {

    public SellCommand() {
        super("sell", SenderType.PLAYER);
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        if (SXAttribute.isVault()) {
            SellInventory.openSellInventory((Player) sender);
        } else {
            sender.sendMessage(Message.getMsg(Message.PLAYER__NO_VAULT));
        }
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        return null;
    }
}
