package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 刷新玩家属性 (重新载入装备源并施加 UPDATE 类属性)
 *
 * @author Ray_Hughes
 */
public class UpdateCommand extends SubCommand {

    public UpdateCommand() {
        super("update");
        setArg(" [Player]");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player target = SourceCommand.resolveTarget(sender, args);
        if (target == null) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
            return;
        }
        SXAttribute.getAttributeManager().loadEntityData(target);
        SXAttribute.getAttributeManager().attributeUpdateEvent(target);
        sender.sendMessage("§a[SX-Attribute] §7已刷新 §e" + target.getName() + " §7的属性");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.contains(args[1])).collect(Collectors.toList());
        }
        return null;
    }
}
