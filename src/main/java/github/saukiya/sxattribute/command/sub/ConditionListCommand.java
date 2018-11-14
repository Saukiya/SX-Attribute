package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Message;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条件标签查询指令
 *
 * @author Saukiya
 */
public class ConditionListCommand extends SubCommand {

    public ConditionListCommand() {
        super("conditionList", SenderType.ALL);
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        String search = args.length < 2 ? "" : args[1];
        int filterSize = 0;
        for (Map.Entry<Integer, SubCondition> entry : plugin.getConditionManager().getConditionMap().entrySet()) {
            String message = "§b" + entry.getKey() + " §7- §c" + entry.getValue().getName() + " §8[§7Plugin: §c" + entry.getValue().getPlugin().getName() + "§8]";
            if (message.contains(search)) {
                if (sender instanceof Player) {
                    List<String> list = new ArrayList<>();
                    list.add("&bConditionType: ");
                    Arrays.stream(entry.getValue().getType()).map(type -> "&7- " + type.getType() + " &8(&7" + type.getName() + "&8)").forEach(list::add);
                    TextComponent tc = Message.getTextComponent(message, null, list);
                    if (entry.getValue().introduction().size() > 0) {
                        tc.addExtra(Message.getTextComponent("§7 - §8[§cIntroduction§8]", null, entry.getValue().introduction()));
                    }
                    ((Player) sender).spigot().sendMessage(tc);
                } else {
                    sender.sendMessage(message);
                }
            } else {
                filterSize++;
            }
        }
        if (search.length() > 0) {
            sender.sendMessage("§7> Filter§c " + filterSize + " §7Conditions");
        }
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            return plugin.getConditionManager().getConditionMap().values().stream().map(SubCondition::getName).collect(Collectors.toList());
        }
        return null;
    }
}
