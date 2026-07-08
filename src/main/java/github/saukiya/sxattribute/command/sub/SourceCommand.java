package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.attribute.AttributeSource;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 查看玩家属性来源 (多属性源)
 *
 * @author Ray_Hughes
 */
public class SourceCommand extends SubCommand {

    public SourceCommand() {
        super("source");
        setArg(" [Player]");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args);
        if (target == null) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
            return;
        }
        Set<String> names = SXAttribute.getAttributeManager().getSourceNames(target.getUniqueId());
        sender.sendMessage("§6===== §e" + target.getName() + " §6属性来源 (§e" + names.size() + "§6) =====");
        if (names.isEmpty()) {
            sender.sendMessage("§7(无任何属性来源)");
            return;
        }
        for (String name : names) {
            AttributeSource source = SXAttribute.getAttributeManager().getSource(target.getUniqueId(), name);
            if (source == null) {
                continue;
            }
            sender.sendMessage("§b[" + name + "]" + (source.isSilent() ? " §8(静态)" : ""));
            List<String> lines = describe(target, source.getData());
            if (lines.isEmpty()) {
                sender.sendMessage("§7 - (空)");
            } else {
                lines.forEach(sender::sendMessage);
            }
        }
    }

    /**
     * 解析目标玩家: 有参数则取该玩家, 否则取发送者自身
     */
    static Player resolveTarget(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return Bukkit.getPlayerExact(args[1]);
        }
        return sender instanceof Player ? (Player) sender : null;
    }

    /**
     * 把一个源的数据渲染成非零属性行 (供 source 指令与 statssource 面板共用)
     */
    static List<String> describe(Player player, SXAttributeData data) {
        List<String> lines = new ArrayList<>();
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            double[] values = data.getValues(attribute);
            for (String placeholder : attribute.getPlaceholders()) {
                Object value = attribute.getPlaceholder(values, player, placeholder);
                if (value == null) {
                    continue;
                }
                String text = value instanceof Double ? SXAttribute.getDf().format((Double) value) : value.toString();
                // 跳过纯零值(如 "0" / "0.0")
                if (text.isEmpty() || text.replaceAll("[^0-9.]", "").matches("0(\\.0+)?")) {
                    continue;
                }
                lines.add("§7 - §f" + placeholder + "§7: §a" + text);
            }
        }
        return lines;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.contains(args[1])).collect(Collectors.toList());
        }
        return null;
    }
}
