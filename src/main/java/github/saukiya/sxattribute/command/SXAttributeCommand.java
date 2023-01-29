package github.saukiya.sxattribute.command;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxitem.command.SubCommand;

import java.util.Arrays;

/**
 * 子指令抽象类
 *
 * @author Saukiya
 */
public abstract class SXAttributeCommand extends SubCommand {

    public SXAttributeCommand(String cmd, int priority) {
        super(cmd, priority);
    }

    protected String permission() {
        return SXAttribute.getInst().getName() + "." + cmd;
    }

    public String getIntroduction() {
        return Arrays.stream(Message.values()).anyMatch(loc -> loc.name().equals("COMMAND__" + cmd.toUpperCase())) ? Message.getMsg(Message.valueOf("COMMAND__" + cmd.toUpperCase())) : "";
    }
}
