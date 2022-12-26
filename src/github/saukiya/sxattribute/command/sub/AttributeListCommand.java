package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.util.Message;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 属性标签查询指令
 *
 * @author Saukiya
 */
public class AttributeListCommand extends SubCommand {

    public AttributeListCommand() {
        super("attributeList");
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

                TextComponent tc = Message.Tool.getTextComponent(message, null, str.toString());
                if (attribute.config() != null) {
                    tc.addExtra(Message.Tool.getTextComponent("§r §7- §r", null, ""));
                    tc.addExtra(Message.Tool.getTextComponent("§8[§cConfig§8]", null, attribute.config().saveToString()));
                }
                Message.Tool.sendTextComponent(sender, tc);
            } else {
                sender.sendMessage(message);
            }
        }
    }
}
