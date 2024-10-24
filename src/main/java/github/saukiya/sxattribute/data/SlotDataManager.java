package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 */
public class SlotDataManager {

    @Getter
    private final List<SlotData> slotList = new ArrayList<>();

    public SlotDataManager() {
        loadData();
    }

    public void loadData() {
        slotList.clear();
        List<String> registerSlotList = Config.getConfig().getStringList(Config.REGISTER_SLOTS_LIST);
        if (Config.isRegisterSlot() && registerSlotList.size() > 0) {
            for (String str : registerSlotList) {
                if (str.contains("#") && str.split("#").length > 1) {
                    String[] args = str.split("#");
                    String name = args[1];
                    int slot = Integer.valueOf(args[0].replaceAll("[^0-9]", ""));
                    this.slotList.add(new SlotData(name, slot));
                }
            }
            SXAttribute.getInst().getLogger().info("Loaded " + this.slotList.size() + " RegisterSlot");
        } else {
            SXAttribute.getInst().getLogger().info("Disable SlotData");
        }
    }
}