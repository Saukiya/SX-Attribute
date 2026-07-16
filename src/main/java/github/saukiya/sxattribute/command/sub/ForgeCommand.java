package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/** 打开统一配置化锻造 GUI。 */
public final class ForgeCommand extends SubCommand {

    public ForgeCommand() {
        super("forge");
        setType(SenderType.PLAYER);
        setArg(" [功能]");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        SXAttribute.getForgeFeatureManager().open((Player) sender, args.length > 1 ? args[1] : null);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 2 ? SXAttribute.getForgeFeatureManager().enabledFeatureIds() : null;
    }

    @Override
    public String getIntroduction() {
        return "打开配置化装备成长界面";
    }
}
