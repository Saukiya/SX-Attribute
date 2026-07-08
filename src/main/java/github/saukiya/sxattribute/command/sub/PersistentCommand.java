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
 * 新增持久化属性源 (存盘, 跨重连/重启存活)
 * <p>
 * 用法: {@code /sxa persistent <Player> <源名> <属性词条...>}
 * 多条属性用英文逗号分隔, 例: {@code /sxa persistent Steve 力量加成 攻击伤害: 50, 暴击几率: 10}
 *
 * @author Ray_Hughes
 */
public class PersistentCommand extends SubCommand {

    public PersistentCommand() {
        super("persistent");
        setArg(" <Player> <Source> <Attr...>");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§c用法: /sxa persistent <玩家> <源名> <属性词条...>  (多条用英文逗号分隔)");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__NO_ONLINE));
            return;
        }
        String source = args[2];
        StringBuilder builder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            if (i > 3) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        List<String> lore = new ArrayList<>();
        for (String part : builder.toString().split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                lore.add(trimmed);
            }
        }
        if (lore.isEmpty()) {
            sender.sendMessage("§c至少需要一条属性词条, 如: 攻击伤害: 50");
            return;
        }
        SXAttribute.getPersistentSourceManager().add(target, source, lore);
        SXAttribute.getApi().addSourceAttribute(target, source, lore, true);
        sender.sendMessage("§a[SX-Attribute] §7已为 §e" + target.getName() + " §7新增持久化源 §b[" + source + "]§7: §f" + lore);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.contains(args[1])).collect(Collectors.toList());
        }
        return null;
    }
}
