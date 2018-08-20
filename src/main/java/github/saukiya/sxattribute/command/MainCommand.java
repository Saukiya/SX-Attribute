package github.saukiya.sxattribute.command;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.sub.*;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Saukiya
 */

public class MainCommand implements CommandExecutor, TabCompleter {

    private final CommandList subCommands = SubCommand.subCommands;

    private SXAttribute plugin;

    public MainCommand(SXAttribute plugin) {
        this.plugin = plugin;
        new StatsCommand();
        new RepairCommand();
        new SellCommand();
        new GiveCommand();
        new SaveCommand();
        new DisplaySlotCommand();
        new NBTCommand();
        new ReloadCommand();
    }

    public void setUp(String command) {
        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(this);
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + subCommands.size() + "§r Commands");
    }

    private SenderType getType(CommandSender sender) {
        if (sender instanceof Player) {
            return SenderType.PLAYER;
        }
        return SenderType.CONSOLE;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String label, String[] args) {
        SenderType type = getType(sender);
        if (args.length == 0) {
            sender.sendMessage("§0-§8 --§7 ---§c ----§4 -----§b " + SXAttribute.getPlugin().getName() + "§4 -----§c ----§7 ---§8 --§0 - §0Author Saukiya");
            String color = "§7";
            for (SubCommand sub : subCommands.toCollection()) {
                if (sub.isUse(sender, type) && !sub.hide()) {
                    color = color.equals("§7") ? "" : "§7";
                    sub.sendIntroduction(sender, color, label);
                }
            }
            return true;
        }
        for (SubCommand sub : subCommands.toCollection()) {
            if (sub.cmd().equalsIgnoreCase(args[0])) {
                if (!sub.isUse(sender, type)) {
                    sender.sendMessage(Message.getMsg(Message.ADMIN__NO_PERMISSION_CMD));
                } else {
                    sub.onCommand(plugin, sender, args);
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
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands.toCollection()) {
                if (subCommand.isUse(sender, type) && !subCommand.hide() && subCommand.cmd().contains(args[0])) {
                    String cmd = subCommand.cmd();
                    list.add(cmd);
                }
            }
            return list;
        } else {
            for (SubCommand subCommand : subCommands.toCollection()) {
                if (subCommand.cmd().equalsIgnoreCase(args[0])) {
                    return Optional.of(subCommand).filter(sub -> sub.isUse(sender, type)).map(sub -> sub.onTabComplete(plugin, sender, args)).orElse(null);
                }
            }
            return Optional.<SubCommand>empty().filter(sub -> sub.isUse(sender, type)).map(sub -> sub.onTabComplete(plugin, sender, args)).orElse(null);
        }
    }
}
