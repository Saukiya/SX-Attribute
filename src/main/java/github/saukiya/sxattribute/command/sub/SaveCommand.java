package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.itemdata.IGenerator;
import github.saukiya.sxattribute.data.itemdata.ItemDataManager;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保存物品指令(非完全保存)
 *
 * @author Saukiya
 */
public class SaveCommand extends SubCommand {
    public SaveCommand() {
        super("save");
        setArg(" <ItemName> [Type]");
        setType(SenderType.PLAYER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_FORMAT));
            return;
        }
        String itemName = args[1];
        Player player = (Player) sender;
        ItemStack itemStack = SXAttribute.getVersionSplit()[1] > 8 ? player.getEquipment().getItemInMainHand() : player.getEquipment().getItemInHand();
        if (itemStack.getType().toString().contains("AIR")) {
            player.sendMessage(Message.getMsg(Message.ADMIN__NO_ITEM));
            return;
        }
        if (SXAttribute.getItemDataManager().hasItem(itemName)) {
            player.sendMessage(Message.getMsg(Message.ADMIN__HAS_ITEM, itemName));
            return;
        }
        try {
            if (SXAttribute.getItemDataManager().saveItem(itemName, itemStack, args.length > 2 ? args[2] : "SX")) {
                sender.sendMessage(Message.getMsg(Message.ADMIN__SAVE_ITEM, itemName));
            } else {
                sender.sendMessage(Message.getMsg(Message.ADMIN__SAVE_NO_TYPE, itemName));
            }
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(Message.getMsg(Message.ADMIN__SAVE_ITEM_ERROR, itemName));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            return ItemDataManager.getGenerators().stream().map(IGenerator::getType).collect(Collectors.toList());
        }
        return null;
    }
}
