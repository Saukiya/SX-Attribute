package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;


/**
 * @author Saukiya
 */
public class MythicMobsDrop extends SubAttribute implements Listener {

    public MythicMobsDrop() {
        super(SXAttribute.getInst(), 0, AttributeType.OTHER);
    }

    @EventHandler
    private void onMythicMobDeathEvent(MythicMobDeathEvent event) {
        if (event.getKiller() instanceof Player) {
            MythicMob mm = event.getMobType();
            List<ItemStack> drops = event.getDrops();
            for (String str : mm.getConfig().getStringList("SX-Drop")) {
                if (str.contains(" ")) {
                    String[] args = str.split(" ");
                    int amount = 1;
                    if (args.length > 2 && args[2].length() > 0 && SXAttribute.getRandom().nextDouble() > Double.parseDouble(args[2])) {
                        continue;
                    }
                    if (args.length > 1 && args[1].length() > 0) {// 数量判断
                        if (args[1].contains("-") && args[1].split("-").length > 1) {
                            int i1 = Integer.parseInt(args[1].split("-")[0]);
                            int i2 = Integer.parseInt(args[1].split("-")[1]);
                            if (i1 > i2) {
                                SXAttribute.getInst().getLogger().warning("MythicMobs - Drop Random Error: " + mm.getDisplayName() + " - " + str);
                            } else {
                                amount = SXAttribute.getRandom().nextInt(i2 - i1 + 1) + i1;
                            }
                        } else {
                            amount = Integer.parseInt(args[1].replaceAll("[^\\d]", ""));
                        }
                    }
                    ItemStack item = SXAttribute.getApi().getItem(args[0], (Player) event.getKiller());
                    if (item != null) {
                        item.setAmount(amount);
                        drops.add(item.clone());
                    } else {
                        SXAttribute.getInst().getLogger().warning("MythicMobs - Drop No Item: " + mm.getDisplayName() + " - " + args[0]);
                    }
                }
            }
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return null;
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return 0;
    }
}