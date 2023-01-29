package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SXAttributeCommand;
import github.saukiya.sxattribute.event.SXReloadEvent;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 重载指令
 *
 * @author Saukiya
 */
public class ReloadCommand extends SXAttributeCommand {

    public ReloadCommand() {
        super("reload", 100);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        long oldTimes = System.currentTimeMillis();
        Config.loadConfig();
        Message.loadMessage();
        TimeUtil.getSdf().reload();
        SXAttribute.getAttributeManager().onAttributeReload();
        SXAttribute.getAttributeManager().loadDefaultAttributeData();
        SXAttribute.getSlotDataManager().loadData();
        int size = 0;
        d1:
        for (UUID uuid : new ArrayList<>(SXAttribute.getAttributeManager().getEntityDataMap().keySet())) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(uuid)) {
                        // 找到了耶 不清理
                        continue d1;
                    }
                }
            }
            // 全部循环没找到 清除
            SXAttribute.getAttributeManager().clearEntityData(uuid);
            size++;
        }

        if (size > 0) {
            sender.sendMessage(Message.getMsg(Message.ADMIN__CLEAR_ENTITY_DATA, String.valueOf(size)));
        }
        sender.sendMessage(Message.getMsg(Message.ADMIN__PLUGIN_RELOAD));
        SXAttribute.getInst().getLogger().info("Reloading Time: " + (System.currentTimeMillis() - oldTimes) + " ms");
        Bukkit.getPluginManager().callEvent(new SXReloadEvent(sender));
    }
}