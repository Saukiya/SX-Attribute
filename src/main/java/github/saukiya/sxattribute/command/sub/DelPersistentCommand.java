package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 删除一个持久化属性源
 * <p>
 * 用法: {@code /sxa del-persistent <Player> <源名>}
 *
 * @author Ray_Hughes
 */
public class DelPersistentCommand extends SubCommand {

    public DelPersistentCommand() {
        super("del-persistent");
        setArg(" <Player> <Source>");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /sxa del-persistent <玩家> <源名>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
            return;
        }
        String source = args[2];
        boolean removed = SXAttribute.getPersistentSourceManager().remove(target, source);
        SXAttribute.getApi().takeSourceAttribute(target, source);
        if (removed) {
            sender.sendMessage("§a[SX-Attribute] §7已删除 §e" + target.getName() + " §7的持久化源 §b[" + source + "]");
        } else {
            sender.sendMessage("§c未找到持久化源 §b[" + source + "]§c (已尝试移除其运行期同名源)");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.contains(args[1])).collect(Collectors.toList());
        }
        if (args.length == 3) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target != null) {
                return new ArrayList<>(SXAttribute.getPersistentSourceManager().getSources(target).keySet())
                        .stream().filter(n -> n.contains(args[2])).collect(Collectors.toList());
            }
        }
        return null;
    }
}
