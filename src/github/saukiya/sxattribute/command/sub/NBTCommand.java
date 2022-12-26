package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NBT显示指令
 *
 * @author Saukiya
 */
public class NBTCommand extends SubCommand {

    public NBTCommand() {
        super("nbt");
        setArg(" <ItemName>");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ItemStack item;
        if (args.length > 1 && SXAttribute.getItemDataManager().hasItem(args[1])) {
            item = SXAttribute.getItemDataManager().getItem(args[1], sender instanceof Player ? (Player) sender : null);
        } else if (sender instanceof Player) {
            EntityEquipment eq = ((Player) sender).getEquipment();
            if (SXAttribute.getVersionSplit()[1] >= 9) {
                item = eq.getItemInMainHand();
            } else {
                item = eq.getItemInHand();
            }
        } else {
            SXAttribute.getItemDataManager().sendItemMapToPlayer(sender);
            return;
        }
        String str = SXAttribute.getNbtUtil().getAllNBT(item);
        sender.sendMessage("\n\n" + str + "\n");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 2 ? SXAttribute.getItemDataManager().getItemList().stream().filter(itemName -> itemName.contains(args[1])).collect(Collectors.toList()) : null;
    }
}
