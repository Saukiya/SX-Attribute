package github.saukiya.sxattribute.command;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Saukiya
 */
public abstract class SubCommand {

    static final CommandList subCommands = new CommandList();

    private String cmd, arg = "";

    private Boolean hide = false;

    private SenderType[] senderTypes = new SenderType[]{SenderType.ALL};

    public SubCommand(String cmd, String arg, Boolean hide, SenderType... senderTypes) {
        this.cmd = cmd;
        this.arg = arg;
        this.hide = hide;
        this.senderTypes = senderTypes;
        subCommands.add(this);
    }

    public SubCommand(String cmd, String arg, SenderType... senderTypes) {
        this.cmd = cmd;
        this.arg = arg;
        this.senderTypes = senderTypes;
        subCommands.add(this);
    }

    public SubCommand(String cmd, SenderType... senderTypes) {
        this.cmd = cmd;
        this.senderTypes = senderTypes;
        subCommands.add(this);
    }

    public SubCommand(String cmd) {
        this.cmd = cmd;
        subCommands.add(this);
    }

    protected String cmd() {
        return cmd;
    }

    protected String arg() {
        return arg;
    }

    protected Boolean hide() {
        return hide;
    }

    private String permission() {
        return SXAttribute.getPlugin().getName() + "." + cmd;
    }

    public abstract void onCommand(SXAttribute plugin, CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args);

    Boolean isUse(CommandSender sender, SenderType type) {
        return sender.hasPermission(permission()) && Arrays.stream(this.senderTypes).anyMatch(senderType -> senderType.equals(SenderType.ALL) || senderType.equals(type));
    }

    public void sendIntroduction(CommandSender sender, String color, String label) {
        String introduction = Message.getMsg(Message.valueOf("COMMAND__" + cmd().toUpperCase()));
        String message = color + MessageFormat.format("/{0} {1}{2}§7 -§c {3}", label, cmd(), arg(), introduction);
        if (sender instanceof Player) {
            Message.sendCommandToPlayer((Player) sender, message, MessageFormat.format("/{0} {1}", label, cmd()), Collections.singletonList("§c" + introduction));
        } else {
            sender.sendMessage(message);
        }
    }

    protected Player getPlayer(CommandSender sender, Player player) {
        if (player == null) {
            if (sender instanceof Player) {
                player = (Player) sender;
            } else {
                sender.sendMessage(Message.getMsg(Message.ADMIN__NO_CONSOLE));
                return null;
            }
        }
        return player;
    }
}
