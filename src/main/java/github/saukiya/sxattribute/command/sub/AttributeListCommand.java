package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SXAttributeCommand;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxitem.util.ComponentBuilder;
import github.saukiya.sxitem.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 属性标签查询指令
 *
 * @author Saukiya
 */
public class AttributeListCommand extends SXAttributeCommand {

    public AttributeListCommand() {
        super("attributeList", 80);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        List<SubAttribute> attributes = SubAttribute.getAttributes();
        SXAttributeData attributeData = sender instanceof Player ? SXAttribute.getAttributeManager().getEntityData((LivingEntity) sender) : null;
        for (SubAttribute attribute : attributes) {
            String message = "§b" + attribute.getPriority() + " §7- §c" + attribute.getName() + " §8[§7Plugin: §c" + attribute.getPlugin().getName() + "§7,Length: §c" + attribute.getLength() + "§8]";
            if (sender instanceof Player) {
                StringBuilder str = new StringBuilder().append("§bName: ").append(attribute.getName()).append("\n§bAttributeType: ");
                for (AttributeType type : attribute.getTypes()) {
                    str.append("\n§7- ").append(type.name());
                }
                if (attribute.getPlaceholders() != null) {
                    System.out.println(attribute.getName());
                    for (String placeName : attribute.getPlaceholders()) {
                        str.append("\n§7- %sx_").append(placeName).append("% : ").append(attribute.getPlaceholder(attributeData.getValues(attribute), (Player) sender, placeName));
                    }
                }
                str.append("\n§bValue: ").append(attribute.calculationCombatPower(attributeData.getValues(attribute)));

                ComponentBuilder builder = MessageUtil.getInst().componentBuilder().add(message).show(str.toString());
                if (attribute.getConfig() != null) {
                    builder.add("§r §7- §r").add("§8[§cConfig§8]").show(attribute.getConfig().saveToString());
                }
                builder.send(sender);
            } else {
                sender.sendMessage(message);
            }
        }
    }
}
