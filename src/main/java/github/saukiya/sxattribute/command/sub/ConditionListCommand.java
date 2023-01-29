package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.command.SXAttributeCommand;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxitem.util.MessageUtil;
import org.bukkit.command.CommandSender;

/**
 * 条件标签查询指令
 *
 * @author Saukiya
 */
public class ConditionListCommand extends SXAttributeCommand {

    public ConditionListCommand() {
        super("conditionList", 90);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        for (SubCondition condition : SubCondition.getConditions()) {
            String message = "§b" + condition.getPriority() + " §7- §c" + condition.getName() + " §8[§7Plugin: §c" + condition.getPlugin().getName() + "§8]";
            StringBuilder str = new StringBuilder().append("\n&bName: ").append(condition.getName()).append("\n&bConditionType: ");
            for (EquipmentType type : condition.getTypes()) {
                str.append("\n&7- ").append(type.name());
            }
            MessageUtil.getInst().componentBuilder().add(message).show(str.toString()).send(sender);
        }
    }
}
