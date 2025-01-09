package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxitem.SXItem;
import github.saukiya.sxitem.helper.MythicMobsHelper;
import github.saukiya.tools.base.EmptyMap;
import github.saukiya.tools.nms.NMS;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Saukiya
 */

public class ListenerMythicMobs {

    @Getter
    private static Listener handler;

    @Getter
    private static Function<Entity, Boolean> isMythicBossBar;

    @Setter
    private static MythicMobsHelper.SpawnHandler spawnHandler = new MythicMobSpawnHandler();

    @Setter
    private static MythicMobsHelper.DeathHandler deathHandler = new MythicMobDeathHandler();

    public static void setup() {
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            if (NMS.hasClass("io.lumine.xikage.mythicmobs.mobs.MythicMob")) {
                Bukkit.getPluginManager().registerEvents(handler = new V4Listener(), SXAttribute.getInst());
                isMythicBossBar = V4Listener::isMythicBossBar;
                SXItem.getInst().getLogger().info("MythicMobsV4Helper Enabled");
            } else if (NMS.hasClass("io.lumine.mythic.api.mobs.MythicMob")) {
                Bukkit.getPluginManager().registerEvents(handler = new V5Listener(), SXAttribute.getInst());
                isMythicBossBar = V5Listener::isMythicBossBar;
                SXItem.getInst().getLogger().info("MythicMobsV5Helper Enabled");
            }
        } else {
            SXItem.getInst().getLogger().info("MythicMobsHelper Disable");
        }
    }

    public static class MythicMobDeathHandler implements MythicMobsHelper.DeathHandler {

        @Override
        public void death(String mobType, Location mobLocation, Map<String, String> mobMap, Player player, List<ItemStack> drops, List<String> sxDropList) {
            for (String str : sxDropList) {
                if (str.contains(" ")) {
                    String[] args = str.split(" ");
                    int amount = 1;
                    if (args.length > 2 && args[2].length() > 0 && SXAttribute.getRandom().nextDouble() > Double.valueOf(args[2].replaceAll("[^0-9.]", ""))) {
                        continue;
                    }
                    if (args.length > 1 && args[1].length() > 0) {// 数量判断
                        if (args[1].contains("-") && args[1].split("-").length > 1) {
                            int i1 = Integer.valueOf(args[1].split("-")[0].replaceAll("[^\\d]", ""));
                            int i2 = Integer.valueOf(args[1].split("-")[1].replaceAll("[^\\d]", ""));
                            if (i1 > i2) {
                                SXAttribute.getInst().getLogger().warning("MythicMobs - Drop Random Error: " + mobType + " - " + str);
                            } else {
                                amount = SXAttribute.getRandom().nextInt(i2 - i1 + 1) + i1;
                            }
                        } else {
                            amount = Integer.valueOf(args[1].replaceAll("[^\\d]", ""));
                        }
                    }
                    ItemStack item = SXAttribute.getApi().getItem(args[0], player);
                    if (item == null) return;
                    item.setAmount(amount);
                    drops.add(item.clone());
                }
            }
        }
    }

    public static class MythicMobSpawnHandler implements MythicMobsHelper.SpawnHandler {

        @Override
        public void spawn(String mobType, EntityEquipment eq, Map<String, String> mobMap, List<String> sxEquipmentList) {
            for (String str : sxEquipmentList) {
                if (str.contains(":")) {
                    String[] args = str.split(":");// 物品:物品位置穿戴位置 几率
                    if (args.length > 1) {
                        String position;
                        //几率判断 args[1] = 0 0.05
                        if (args[1].contains(" ")) {
                            String[] argsSplit = args[1].split(" ");
                            if (argsSplit.length > 1 && SXAttribute.getRandom().nextDouble() > Double.valueOf(argsSplit[1])) {
                                continue;
                            } else {
                                position = argsSplit[0];
                            }
                        } else {
                            position = args[1];
                        }

                        ItemStack item = SXAttribute.getItemDataManager().getItem(args[0], null);
                        if (item == null) return;
                        switch (position) {
                            case "-1":
                            case "OFFHAND":
                                eq.setItemInOffHand(item);
                                break;
                            case "0":
                            case "HAND":
                                eq.setItemInMainHand(item);
                                break;
                            case "1":
                            case "FEET":
                                eq.setBoots(item);
                                break;
                            case "2":
                            case "LEGS":
                                eq.setLeggings(item);
                                break;
                            case "3":
                            case "CHEST":
                                eq.setChestplate(item);
                                break;
                            case "4":
                            case "HEAD":
                                eq.setHelmet(item);
                                break;
                            default:
                                SXAttribute.getInst().getLogger().warning("MythicMobs - Equipment Error: " + mobType + " - " + str);
                        }
                    }
                }
            }
        }
    }

    public static class V4Listener implements Listener {

        private boolean isVersionGreaterThan490;

        public static boolean isMythicBossBar(Entity entity) {
            io.lumine.xikage.mythicmobs.mobs.ActiveMob activeMob = io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMythicMobInstance(entity);
            return activeMob != null && activeMob.getType().usesBossBar();
        }

        V4Listener() {
            try {
                io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent.class.getMethod("getMob");
                isVersionGreaterThan490 = true;
            } catch (NoSuchMethodException e) {
                isVersionGreaterThan490 = false;
            }
        }

        @EventHandler
        void on(io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent event) {
            if (event.getEntity() instanceof LivingEntity) {
                String mobType = event.getMobType().getInternalName();
                EntityEquipment mobEquipment = ((LivingEntity) event.getEntity()).getEquipment();
                Map<String, String> mobMap = isVersionGreaterThan490 ? getMobMap(event.getMob()) : EmptyMap.emptyMap();
                List<String> sxEquipmentList = event.getMobType().getConfig().getStringList("SX-Equipment");
                spawnHandler.spawn(mobType, mobEquipment, mobMap, sxEquipmentList);
            }
        }

        @EventHandler
        void on(io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent event) {
            if (event.getKiller() instanceof Player) {
                String mobType = event.getMobType().getInternalName();
                Location mobLocation = event.getEntity().getLocation();
                Map<String, String> mobMap = getMobMap(event.getMob());
                Player player = (Player) event.getKiller();
                List<ItemStack> drops = event.getDrops();
                List<String> sxDropList = event.getMobType().getConfig().getStringList("SX-Drop");
                sxDropList.addAll(event.getMobType().getConfig().getStringList("SX-Drops"));
                deathHandler.death(mobType, mobLocation, mobMap, player, drops, sxDropList);
                event.setDrops(drops);
            }
        }

        /**
         * 依据 io.lumine.xikage.mythicmobs.mobs.ActiveMob 提供变量
         *
         * @param mob
         */
        public static Map<String, String> getMobMap(io.lumine.xikage.mythicmobs.mobs.ActiveMob mob) {
            Map<String, String> map = new HashMap<>();
            map.put("mob_level", Double.toString(mob.getLevel()));
            map.put("mob_name_display", mob.getDisplayName());
            map.put("mob_name_internal", mob.getType().getInternalName());
            map.put("mob_uuid", mob.getUniqueId().toString());
            return map;
        }
    }

    public static class V5Listener implements Listener {

        public static boolean isMythicBossBar(Entity entity) {
            io.lumine.mythic.core.mobs.ActiveMob activeMob = io.lumine.mythic.bukkit.MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
            return activeMob != null && activeMob.getType().usesBossBar();
        }

        @EventHandler
        void on(io.lumine.mythic.bukkit.events.MythicMobSpawnEvent event) {
            if (event.getEntity() instanceof LivingEntity) {
                String mobType = event.getMobType().getInternalName();
                EntityEquipment mobEquipment = ((LivingEntity) event.getEntity()).getEquipment();
                Map<String, String> mobMap = getMobMap(event.getMob());
                List<String> sxEquipmentList = event.getMobType().getConfig().getStringList("SX-Equipment");
                spawnHandler.spawn(mobType, mobEquipment, mobMap, sxEquipmentList);
            }
        }

        @EventHandler
        void on(io.lumine.mythic.bukkit.events.MythicMobDeathEvent event) {
            if (event.getKiller() instanceof Player) {
                String mobType = event.getMobType().getInternalName();
                Location mobLocation = event.getEntity().getLocation();
                Map<String, String> mobMap = getMobMap(event.getMob());
                Player player = (Player) event.getKiller();
                List<ItemStack> drops = event.getDrops();
                List<String> sxDropList = event.getMobType().getConfig().getStringList("SX-Drop");
                sxDropList.addAll(event.getMobType().getConfig().getStringList("SX-Drops"));
                deathHandler.death(mobType, mobLocation, mobMap, player, drops, sxDropList);
                event.setDrops(drops);
            }
        }

        /**
         * 依据 io.lumine.mythic.core.mobs.ActiveMob 提供变量
         *
         * @param mob
         */
        public static Map<String, String> getMobMap(io.lumine.mythic.core.mobs.ActiveMob mob) {
            Map<String, String> map = new HashMap<>();
            map.put("mob_level", Double.toString(mob.getLevel()));
            map.put("mob_name_display", mob.getDisplayName());
            map.put("mob_name_internal", mob.getType().getInternalName());
            map.put("mob_uuid", mob.getUniqueId().toString());
            return map;
        }
    }
}