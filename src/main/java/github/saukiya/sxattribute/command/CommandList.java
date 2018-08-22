package github.saukiya.sxattribute.command;

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
                subCommands.put(entry.getKey(), subCommand);
                return;
            }
        }
        subCommands.put(subCommands.size() + 1, subCommand);
    }

    public int size() {
        return subCommands.size();
    }

    public Collection<SubCommand> toCollection() {
        return subCommands.values();
    }
}
