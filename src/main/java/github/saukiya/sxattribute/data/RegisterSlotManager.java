package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Saukiya
 */
public class RegisterSlotManager {

    @Getter
    private final Map<Integer, RegisterSlot> registerSlotMap = new HashMap<>();

    @Getter
    private final SXAttribute plugin;

    public RegisterSlotManager(SXAttribute plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        registerSlotMap.clear();
        List<String> registerSlotList = Config.getConfig().getStringList(Config.REGISTER_SLOTS_LIST);
        if (Config.isRegisterSlot() && registerSlotList.size() > 0) {
            for (String str : registerSlotList) {
                if (str.contains("#") && str.split("#").length > 1) {
                    String[] args = str.split("#");
                    String name = args[1];
                    int slot = Integer.valueOf(args[0].replaceAll("[^0-9]", ""));
                    String id = args.length > 2 ? args[2] : null;
                    registerSlotMap.put(slot, new RegisterSlot(slot, name, id, plugin.getItemUtil()));
                }
            }
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + registerSlotMap.size() + " §rRegisterSlot");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cDisable RegisterSlot");
        }
    }
}
