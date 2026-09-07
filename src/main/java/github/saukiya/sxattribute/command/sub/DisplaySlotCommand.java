package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.SlotData;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 指出玩家自身背包中的饰品位置；槽位编号与属性加载使用同一份注册数据。
 * 只提供位置指引，避免复制背包或放入占位物品影响真实装备与属性计算。
 */
public class DisplaySlotCommand extends SubCommand {

    /** 保留旧命令名称，使历史用法和对应权限继续有效。 */
    public DisplaySlotCommand() {
        super("displaySlot");
        setType(SenderType.PLAYER);
    }

    /** 将 Bukkit 槽位转换为玩家背包中的位置，饰品仍由玩家在原背包内放入、取出。 */
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // 属性加载在 RPGInventory 模式下走独立分支，不能指引玩家使用当前未被扫描的原背包槽位。
        if (SXAttribute.isRpgInventory()) {
            sender.sendMessage(Message.getMsg(Message.PLAYER__SLOT_RPG_INVENTORY));
            return;
        }
        Player player = (Player) sender;
        List<SlotData> slots = SXAttribute.getSlotDataManager().getSlotList().stream()
                .filter(slot -> slot.getSlot() >= 0 && slot.getSlot() < player.getInventory().getSize())
                .collect(Collectors.toList());
        if (slots.isEmpty()) {
            sender.sendMessage(Message.getMsg(Message.PLAYER__NO_REGISTER_SLOTS));
            return;
        }
        sender.sendMessage(Message.getMsg(Message.PLAYER__SLOT_GUIDE));
        for (SlotData slot : slots) {
            int index = slot.getSlot();
            // PlayerInventory 使用 0..8 快捷栏、9..35 背包三排；这不是容器窗口的 rawSlot。
            if (index < 9) {
                sender.sendMessage(Message.getMsg(Message.PLAYER__SLOT_HOTBAR,
                        slot.getName(), index, index + 1));
            } else if (index < 36) {
                sender.sendMessage(Message.getMsg(Message.PLAYER__SLOT_STORAGE,
                        slot.getName(), index, index / 9, index % 9 + 1));
            } else {
                // 兼容已有护甲/副手注册配置，不能把这些槽位误报成背包第四排。
                sender.sendMessage(Message.getMsg(Message.PLAYER__SLOT_EQUIPMENT, slot.getName(), index));
            }
        }
    }
}
