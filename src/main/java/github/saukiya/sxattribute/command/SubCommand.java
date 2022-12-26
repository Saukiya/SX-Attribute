package github.saukiya.sxattribute.command;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 子指令抽象类
 *
 * @author Saukiya
 */
public abstract class SubCommand implements Comparable<SubCommand> {

    static final List<SubCommand> commands = new ArrayList<>();

    String cmd, arg = "";

    boolean hide = false;

    private SenderType[] types = new SenderType[]{SenderType.ALL};

    private int priority = 100;

    /**
     * @param cmd String
     */
    public SubCommand(String cmd) {
        this.cmd = cmd;
    }

    /**
     * 注册指令方法
     */
    public final void registerCommand() {
        if (!SXAttribute.getInst().isEnabled() && commands.stream().noneMatch(subCommand -> subCommand.cmd.equals(cmd))) {
            int index = IntStream.range(0, commands.size()).filter(i -> commands.get(i).cmd.equals(this.cmd)).findFirst().orElse(-1);
            if (index < 0) {
                commands.add(this);
            } else {
                commands.set(index, this);
            }
        }
    }

    String permission() {
        return SXAttribute.getInst().getName() + "." + cmd;
    }

    /**
     * 执行指令抽象方法
     *
     * @param sender CommandSender
     * @param args   String[]
     */
    public abstract void onCommand(CommandSender sender, String[] args);

    /**
     * TAB执行方法
     *
     * @param sender CommandSender
     * @param args   String[]
     * @return List
     */
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    /**
     * 判断是否可用
     *
     * @param sender CommandSender
     * @param type   SenderType
     * @return boolean
     */
    public boolean isUse(CommandSender sender, SenderType type) {
        return sender.hasPermission(permission()) && Arrays.stream(this.types).anyMatch(senderType -> senderType.equals(SenderType.ALL) || senderType.equals(type));
    }

    public String getIntroduction() {
        return Arrays.stream(Message.values()).anyMatch(loc -> loc.name().equals("COMMAND__" + cmd.toUpperCase())) ? Message.getMsg(Message.valueOf("COMMAND__" + cmd.toUpperCase())) : "";
    }

    protected void setArg(String arg) {
        this.arg = arg;
    }

    protected void setHide() {
        this.hide = true;
    }

    protected void setType(SenderType... types) {
        this.types = types;
    }

    protected void setPriority(int priority) {
        this.priority = priority;
        Collections.sort(commands);
    }

    @Override
    public int compareTo(SubCommand cmd) {
        return Integer.compare(priority, cmd.priority);
    }
}
