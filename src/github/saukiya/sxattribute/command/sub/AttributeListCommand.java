package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.util.Message;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 属性标签查询指令
 *
 * @author Saukiya
 */
public class AttributeListCommand extends SubCommand {

    public AttributeListCommand() {
        super("attributeList", SenderType.ALL);
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        String search = args.length < 2 ? "" : args[1];
        int filterSize = 0;
        SXAttributeData attributeData = sender instanceof Player ? plugin.getAttributeManager().getEntityData((LivingEntity) sender) : new SXAttributeData();
        for (Map.Entry<Integer, SubAttribute> entry : attributeData.getAttributeMap().entrySet()) {
            String message = "§b" + entry.getKey() + " §7- §c" + entry.getValue().getName() + " §8[§7Plugin: §c" + entry.getValue().getPlugin().getName() + "§7,Length: §c" + entry.getValue().getAttributes().length + "§8]";
            if (message.contains(search)) {
                if (sender instanceof Player) {
                    List<String> list = new ArrayList<>();
                    list.add("&bAttributeType: ");
                    for (SXAttributeType type : entry.getValue().getType()) {
                        list.add("&7- " + type.getType() + " &8(&7" + type.getName() + "&8)");
                    }
                    list.add("&bPlaceholders: ");
                    if (entry.getValue().getPlaceholders() != null) {
                        for (String placeName : entry.getValue().getPlaceholders()) {
                            list.add("&7- %sx_" + placeName + "% : " + entry.getValue().getPlaceholder((Player) sender, placeName));
                        }
                    }
                    list.add("&bValue: " + entry.getValue().getValue());
                    TextComponent tc = Message.getTextComponent(message, null, list);
                    if (entry.getValue().introduction().size() > 0) {
                        tc.addExtra(Message.getTextComponent("§7 - §8[§cIntroduction§8]", null, entry.getValue().introduction()));
                    }
                    ((Player) sender).spigot().sendMessage(tc);
                } else {
                    sender.sendMessage(message);
                }
            } else {
                filterSize++;
            }
        }
        if (search.length() > 0) {
            sender.sendMessage("§7> Filter§c " + filterSize + " §7Conditions");
        }
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            return new SXAttributeData().getAttributeMap().values().stream().map(SubAttribute::getName).collect(Collectors.toList());
        }
        return null;
    }
}
