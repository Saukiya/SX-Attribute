package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.event.SXReloadEvent;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 重载指令
 *
 * @author Saukiya
 */
public class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("reload");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        long oldTimes = System.currentTimeMillis();
        Config.loadConfig();
        github.saukiya.sxattribute.util.AttributeConfig.load();
        Message.loadMessage();
        SXAttribute.getRandomStringManager().loadData();
        SXAttribute.getItemDataManager().loadItemData();
        TimeUtil.getSdf().reload();
        SXAttribute.getAttributeManager().onAttributeReload();
        SXAttribute.getAttributeManager().loadDefaultAttributeData();
        SXAttribute.getSlotDataManager().loadData();
        // 重载后立即对在线玩家重新载入并施加属性: UPDATE 类属性按新公式/数值以默认值绝对重算,
        // 使改动即时生效, 避免旧基值残留(否则需玩家切换物品才刷新, 表现为"体型不变/默认值残留")。
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            SXAttribute.getAttributeManager().loadEntityData(onlinePlayer);
            SXAttribute.getAttributeManager().attributeUpdateEvent(onlinePlayer);
        }
        int size = 0;
        d1:
        for (UUID uuid : new ArrayList<>(SXAttribute.getAttributeManager().getTrackedEntities())) {
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