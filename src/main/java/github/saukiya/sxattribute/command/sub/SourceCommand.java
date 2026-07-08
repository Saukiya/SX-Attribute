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
        SXAttributeData defaultData = SXAttribute.getAttributeManager().getDefaultAttributeData();
        boolean hasDefault = defaultData != null && defaultData.isValid();
        int total = names.size() + (hasDefault ? 1 : 0);
        sender.sendMessage("§6===== §e" + target.getName() + " §6属性来源 (§e" + total + "§6) =====");
        if (total == 0) {
            sender.sendMessage("§7(无任何属性来源)");
            return;
        }
        for (String name : names) {
            AttributeSource source = SXAttribute.getAttributeManager().getSource(target.getUniqueId(), name);
            if (source == null) {
                continue;
            }
            printSource(sender, "§b[" + name + "]" + (source.isSilent() ? " §8(静态)" : ""), source.getData());
        }
        // 默认属性(Config.DefaultAttribute)作为独立来源展示, 不并入其它源
        if (hasDefault) {
            printSource(sender, "§b[默认] §8(全局)", defaultData);
        }
    }

    private static void printSource(CommandSender sender, String title, SXAttributeData data) {
        sender.sendMessage(title);
        List<String> lines = describe(data);
        if (lines.isEmpty()) {
            sender.sendMessage("§7 - (空)");
        } else {
            lines.forEach(sender::sendMessage);
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
     * 把一个源渲染成非零属性行 (供 source 指令与 statssource 面板共用)。
     * <p>
     * 用该源<b>自身的原始数值</b>渲染, 准确反映"这个源贡献了多少", 而非实体的实时/合计值。
     */
    static List<String> describe(SXAttributeData data) {
        List<String> lines = new ArrayList<>();
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            double[] values = data.getValues(attribute);
            boolean any = false;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    builder.append(" / ");
                }
                builder.append(SXAttribute.getDf().format(values[i]));
                if (values[i] != 0) {
                    any = true;
                }
            }
            if (any) {
                lines.add("§7 - §f" + attribute.getName() + "§7: §a" + builder);
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
