package github.saukiya.sxattribute.command;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.sub.*;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxitem.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 指令管理器
 *
 * @author Saukiya
 */

public class MainCommand implements CommandExecutor, TabCompleter {

    public MainCommand() {
        new StatsCommand().registerCommand();
        new RepairCommand().registerCommand();
        new SellCommand().registerCommand();
        new AttributeListCommand().registerCommand();
        new ConditionListCommand().registerCommand();
        new ReloadCommand().registerCommand();
    }

    public void setup(String command) {
        SXAttribute.getInst().getCommand(command).setExecutor(this);
        SXAttribute.getInst().getCommand(command).setTabCompleter(this);
        SubCommand.commands.stream().filter(subCommand -> subCommand instanceof Listener).forEach(subCommand -> Bukkit.getPluginManager().registerEvents((Listener) subCommand, SXAttribute.getInst()));
        SXAttribute.getInst().getLogger().info("Loaded " + SubCommand.commands.size() + " Commands");
    }


    private SenderType getType(CommandSender sender) {
        return sender instanceof Player ? SenderType.PLAYER : SenderType.CONSOLE;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String label, String[] args) {
        SenderType type = getType(sender);
        if (args.length == 0) {
            sender.sendMessage("\n§0-§8 --§7 ---§c ----§4 -----§b SX-Attribute§4 -----§c ----§7 ---§8 --§0 - §0Author Saukiya");
            for (int i = 0; i < SubCommand.commands.size(); i++) {
                SubCommand sub = SubCommand.commands.get(i);
                if (sub.isUse(sender, type) && !sub.hide) {
                    MessageUtil.getInst().componentBuilder()
                            .add(i % 2 == 0 ? "" : "§7")
                            .add(MessageFormat.format("/{0} {1}{2}§7 -§c {3}", label, sub.cmd, sub.arg, sub.getIntroduction()))
                            .runCommand(MessageFormat.format("/{0} {1}", label, sub.cmd))
                            .show(sender.isOp() ? "§8§oPermission: " + sub.permission() : null)
                            .send(sender);
                }
            }
            sender.sendMessage("");
            return true;
        }
        for (SubCommand sub : SubCommand.commands) {
            if (sub.cmd.equalsIgnoreCase(args[0])) {
                if (!sub.isUse(sender, type)) {
                    sender.sendMessage(Message.getMsg(Message.ADMIN__NO_PERMISSION_CMD));
                } else {
                    sub.onCommand(sender, args);
                }
                return true;
            }
        }
        sender.sendMessage(Message.getMsg(Message.ADMIN__NO_CMD, args[0]));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        SenderType type = getType(sender);
        return args.length == 1 ? SubCommand.commands.stream().filter(sub -> sub.isUse(sender, type) && !sub.hide && sub.cmd.contains(args[0])).map(sub -> sub.cmd).collect(Collectors.toList()) : SubCommand.commands.stream().filter(sub -> sub.cmd.equalsIgnoreCase(args[0])).findFirst().filter(sub -> sub.isUse(sender, type)).map(sub -> sub.onTabComplete(sender, args)).orElse(null);
    }
}