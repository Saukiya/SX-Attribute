package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

/**
 * 保存物品指令(非完全保存)
 *
 * @author Saukiya
 */
public class SaveCommand extends SubCommand {
    public SaveCommand() {
        super("save", " <ItemName> ", SenderType.PLAYER);
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_FORMAT));
            return;
        }
        String itemName = args[1];
        Player player = (Player) sender;
        ItemStack itemStack = player.getEquipment().getItemInMainHand();
        if (itemStack.getType().toString().contains("AIR")) {
            player.sendMessage(Message.getMsg(Message.ADMIN__NO_ITEM));
            return;
        }
        if (plugin.getItemDataManager().isItem(itemName)) {
            player.sendMessage(Message.getMsg(Message.ADMIN__HAS_ITEM, itemName));
            return;
        }
        try {
            if (args.length > 2 && args[2].equalsIgnoreCase("-a")) {
                plugin.getItemDataManager().importItem(itemName, itemStack);
            } else {
                plugin.getItemDataManager().saveItem(itemName, itemStack);
            }
            sender.sendMessage(Message.getMsg(Message.ADMIN__SAVE_ITEM, itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name(), args[1]));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(Message.getMsg(Message.ADMIN__SAVE_ITEM_ERROR, itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name(), args[1]));
        }
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        return null;
    }
}
