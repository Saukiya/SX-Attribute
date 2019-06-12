package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Saukiya
 */

public class OnMythicmobsSpawnListener implements Listener {

    private final SXAttribute plugin;

    public OnMythicmobsSpawnListener(SXAttribute plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onMythicMobSpawnEvent(MythicMobSpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            MythicMob mm = event.getMobType();
            List<String> list = mm.getEquipment();
            LivingEntity entity = (LivingEntity) event.getEntity();
            EntityEquipment eq;
            eq = entity.getEquipment();
            for (String str : list) {
                // - sx 默认一:0 0.5
                if (str.contains(":") && str.split(":")[0].contains(" ")) {
                    String[] args1 = str.split(":");// 物品位置掉落几率 几率
                    String[] args2 = args1[0].split(" "); // sx 物品编号
                    if (args2[0].equalsIgnoreCase("sx") && args1.length > 1 && args2.length > 1) {
                        int position = 0;
                        String strSplitReplaceAll = args1[1].replaceAll("[^0-9]", "");
                        if (args1[1].contains(" ")) {
                            String[] args1Split = args1[1].split(" ");
                            if (args1Split.length > 1 && args1Split[1].replaceAll("[^0-9.]", "").length() > 0 && SXAttribute.getRandom().nextDouble() > Double.valueOf(args1Split[1].replaceAll("[^0-9.]", ""))) {
                                continue;
                            } else {
                                strSplitReplaceAll = args1Split[0].replace("[^0-9]", "");
                            }
                        }
                        if (strSplitReplaceAll.length() == 1) {
                            position = Integer.valueOf(strSplitReplaceAll);
                        }
                        ItemStack item = plugin.getItemDataManager().getItem(args2[1], null);
                        if (item != null) {
                            if (position >= -1 && position < 5) {
                                switch (position) {
                                    case -1: {
                                        eq.setItemInOffHand(item);
                                        break;
                                    }
                                    case 0: {
                                        eq.setItemInMainHand(item);
                                        break;
                                    }
                                    case 1: {
                                        eq.setBoots(item);
                                        break;
                                    }
                                    case 2: {
                                        eq.setLeggings(item);
                                        break;
                                    }
                                    case 3: {
                                        eq.setChestplate(item);
                                        break;
                                    }
                                    case 4: {
                                        eq.setHelmet(item);
                                        break;
                                    }
                                }
                            } else {
                                Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cMythicmobs怪物: §4" + mm.getDisplayName() + "§c 的物品: §4" + args2[1] + " §c穿戴位置不合法!");
                            }
                        } else {
                            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cMythicmobs怪物: §4" + mm.getDisplayName() + "§1 不存在这个穿戴物品: §4" + args2[1]);
                        }
                    }
                }
            }
        }
    }
}
