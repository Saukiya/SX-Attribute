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
        subCommands.put(subCommands.size() + 1, subCommand);
    }

    public int size() {
        return subCommands.size();
    }

    public Collection<SubCommand> toCollection() {
        return subCommands.values();
    }
}
