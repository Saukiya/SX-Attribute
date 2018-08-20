package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxseal.SXSeal;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Saukiya
 * @since 2018年5月2日
 */

public class OnMythicmobsDropOrSpawnListener implements Listener {

    private final SXAttribute plugin;

    public OnMythicmobsDropOrSpawnListener(SXAttribute plugin) {
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
                        ItemStack item = plugin.getItemDataManager().getItem(args2[1]);
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

    @EventHandler
    void onMythicMobDeathEvent(MythicMobDeathEvent event) {
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
                        ItemStack item = plugin.getItemDataManager().getItem(args[1], (Player) event.getKiller());
                        if (item != null) {
                            if (str.contains("seal") && Bukkit.getPluginManager().isPluginEnabled("SX-Seal")) {
                                SXSeal.getApi().sealItem(item);
                            }
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
}
