package github.saukiya.sxattribute.command;

import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Saukiya
 */
public class CommandList {
    private final Map<Integer, SubCommand> subCommands = new TreeMap<>();

    public void add(SubCommand subCommand) {
        for (Map.Entry<Integer, SubCommand> entry : subCommands.entrySet()) {
            if (entry.getValue().cmd().equalsIgnoreCase(subCommand.cmd())) {
                Bukkit.getConsoleSender().sendMessage("[" + subCommand.getPlugin().getName() + "] Command >> The §c" + subCommand.cmd() + " §rCover To §c" + entry.getValue().cmd() + " §7[§c" + entry.getValue().getPlugin() + "§7]§r !");
                subCommands.put(entry.getKey(), subCommand);
                return;
            }
        }
        subCommands.put(subCommands.size() + 1, subCommand);
    }

    public int size() {
        return subCommands.size();
    }

    public boolean contains(SubCommand subCommand) {
        return subCommands.values().stream().anyMatch(cmd -> cmd.cmd().contains(subCommand.cmd()));
    }

    public Collection<SubCommand> toCollection() {
        return subCommands.values();
    }
}
