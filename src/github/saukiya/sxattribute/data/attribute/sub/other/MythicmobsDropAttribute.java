package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Message;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 */
public class MythicmobsDropAttribute extends SubAttribute implements Listener {

    private static boolean sxSeal = false;

    public MythicmobsDropAttribute() {
        super("MythicmobsDrop", 0, new SXAttributeType(SXAttributeType.Type.OTHER, "Drops"));
    }

    @EventHandler
    private static void onMythicMobDeathEvent(MythicMobDeathEvent event) {
        MythicMob mm = event.getMobType();
        List<String> dropList = mm.getDrops();
        List<ItemStack> drops = event.getDrops();
        if (event.getKiller() instanceof Player) {
            for (String str : dropList) {
                if (str.contains(" ")) {
                    String[] args = str.split(" ");
                    if (args.length > 1 && args[0].equalsIgnoreCase("sx")) {
                        int amount = 1;
                        if (args.length > 3 && args[3].length() > 0 && SXAttribute.getRandom().nextDouble() > Double.valueOf(args[3].replaceAll("[^0-9.]", ""))) {// 几率判断
                            continue;
                        }
                        if (args.length > 2 && args[2].length() > 0) {// 数量判断
                            if (args[2].contains("-") && args[2].split("-").length > 1) {
                                int i1 = Integer.valueOf(args[2].split("-")[0].replaceAll("[^0-9]", ""));
                                int i2 = Integer.valueOf(args[2].split("-")[1].replaceAll("[^0-9]", ""));
                                if (i1 > i2) {
                                    Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c随机数大小不正确!: §4" + str);
                                } else {
                                    amount = SXAttribute.getRandom().nextInt(i2 - i1 + 1) + i1;
                                }
                            } else {
                                amount = Integer.valueOf(args[2].replaceAll("[^0-9]", ""));
                            }
                        }
                        ItemStack item = SXAttribute.getApi().getItem(args[1], (Player) event.getKiller());
                        if (item != null) {
                            item.setAmount(amount);
                            drops.add(item.clone());
                        } else {
                            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cMythicmobs怪物: §4" + mm.getDisplayName() + "§c 不存在这个掉落物品: §4" + args[1]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            if (Bukkit.getPluginManager().getPlugin("SX-Seal") != null) {
                sxSeal = true;
            }
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }
    }

    @Override
    public void eventMethod(EventData eventData) {
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return new ArrayList<>();
    }

    @Override
    public boolean loadAttribute(String lore) {
        return false;
    }

    @Override
    public double getValue() {
        return 0;
    }
}
