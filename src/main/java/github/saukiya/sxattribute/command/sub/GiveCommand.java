package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
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
        super("give");
        setArg(" <ItemName> <Player> <Amount>");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            SXAttribute.getItemDataManager().sendItemMapToPlayer(sender, sender instanceof Player ? new String[]{""} : new String[0]);
            return;
        }
        Player player = null;
        if (!SXAttribute.getItemDataManager().hasItem(args[1])) {
            SXAttribute.getItemDataManager().sendItemMapToPlayer(sender, args[1]);
            return;
        }
        int amount = args.length > 3 ? Integer.valueOf(args[3].replaceAll("[^\\d]", "")) : 1;
        if (args.length > 2) {
            player = Bukkit.getPlayerExact(args[2]);
            if (player == null) {
                sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player != null) {
            for (int i = 0; i < amount; i++) {
                player.getInventory().addItem(SXAttribute.getItemDataManager().getItem(args[1], player));
            }
            SXAttribute.getAttributeManager().loadEntityData(player);
            SXAttribute.getAttributeManager().attributeUpdateEvent(player);
            sender.sendMessage(Message.getMsg(Message.ADMIN__GIVE_ITEM, player.getName(), String.valueOf(amount), args[1]));
        } else {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_CONSOLE));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return SXAttribute.getItemDataManager().getItemList().stream().filter(itemName -> itemName.contains(args[1])).collect(Collectors.toList());
        }
        if (args.length == 4) {
            return Collections.singletonList("1");
        }
        return null;
    }
}
