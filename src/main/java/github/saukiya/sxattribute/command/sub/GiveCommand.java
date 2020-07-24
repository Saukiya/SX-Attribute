package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 给予物品指令
 *
 * @author Saukiya
 */
public class GiveCommand extends SubCommand {

    public GiveCommand() {
        super("give", " <ItemName> <Player> <Amount>", SenderType.ALL);
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getItemDataManager().sendItemMapToPlayer(sender);
            return;
        }
        Player player = null;
        if (plugin.getItemDataManager().getItem(args[1], null) == null) {
            plugin.getItemDataManager().sendItemMapToPlayer(sender, args[1]);
            return;
        }
        int amount = 1;
        if (args.length > 2) {
            player = Bukkit.getPlayerExact(args[2]);
            if (player != null) {
                if (args.length > 3) {
                    amount = Integer.valueOf(args[3].replaceAll("[^0-9]", ""));
                }
            } else {
                sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
                return;
            }
        }
        player = getPlayer(sender, player);
        if (player != null) {
            for (int i = 0; i < amount; i++) {
                player.getInventory().addItem(plugin.getItemDataManager().getItem(args[1], player));
            }
            plugin.getAttributeManager().loadHandData(player);
            sender.sendMessage(Message.getMsg(Message.ADMIN__GIVE_ITEM, player.getName(), String.valueOf(amount), args[1]));
        } else {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_CONSOLE));
        }
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[1].length() == 0) plugin.getItemDataManager().sendItemMapToPlayer(sender);
            return plugin.getItemDataManager().getItemList().stream().filter(itemName -> itemName.contains(args[1])).collect(Collectors.toList());
        }
        if (args.length == 4) {
            return Collections.singletonList("1");
        }
        return null;
    }
}
