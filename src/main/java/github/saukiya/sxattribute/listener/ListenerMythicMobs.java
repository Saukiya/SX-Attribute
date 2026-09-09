package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.hook.mythic.MobSpawnAttributes;
import github.saukiya.sxattribute.hook.mythic.Mythic4Skills;
import github.saukiya.sxattribute.hook.mythic.Mythic5Skills;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxitem.SXItem;
import github.saukiya.sxitem.helper.MythicMobsHelper;
import github.saukiya.tools.nms.NMS;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    /** 启动期按 API 包选择适配器，保证 MM 4/5 及未安装 MM 的服务器互不链接对方类型。 */
    public static void setup() {
        if (!Config.isMythicMobs()) return;
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            if (NMS.hasClass("io.lumine.xikage.mythicmobs.mobs.MythicMob")) {
                Bukkit.getPluginManager().registerEvents(handler = new V4Listener(), SXAttribute.getInst());
                Bukkit.getPluginManager().registerEvents(new Mythic4Skills(), SXAttribute.getInst());
                isMythicBossBar = V4Listener::isMythicBossBar;
                SXItem.getInst().getLogger().info("MythicMobsV4Helper Enabled");
            } else if (NMS.hasClass("io.lumine.mythic.api.mobs.MythicMob")) {
                Bukkit.getPluginManager().registerEvents(handler = new V5Listener(), SXAttribute.getInst());
                Bukkit.getPluginManager().registerEvents(new Mythic5Skills(), SXAttribute.getInst());
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

        public static boolean isMythicBossBar(Entity entity) {
            io.lumine.xikage.mythicmobs.mobs.ActiveMob activeMob = io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMythicMobInstance(entity);
            return activeMob != null && activeMob.getType().usesBossBar();
        }

        /** 出生事件没有统一的 Cancellable 接口，显式检查取消状态；MM 4.1 不要求存在 getMob()。 */
        @EventHandler(priority = EventPriority.MONITOR)
        void on(io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) return;
            LivingEntity entity = (LivingEntity) event.getEntity();
            String mobType = event.getMobType().getInternalName();
            try {
                io.lumine.xikage.mythicmobs.io.MythicConfig config = event.getMobType().getConfig();
                List<String> attributes = MobSpawnAttributes.read(config::isSet, config::getStringList);
                List<String> equipment = new java.util.ArrayList<>(config.getStringList("SX-Equipment"));
                MobSpawnAttributes.spawn(entity, mobType, MobSpawnAttributes.level(event), attributes,
                        variables -> spawnHandler.spawn(mobType, entity.getEquipment(), variables, equipment));
            } catch (RuntimeException | LinkageError exception) {
                MobSpawnAttributes.warn(mobType, exception.toString());
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
         * MM 4 早期 getLevel 返回 int，后期返回 double；早期 ActiveMob 也没有 getDisplayName。
         * 按方法能力读取这两个字段，避免装备/掉落联动在旧服先于技能发生 NoSuchMethodError。
         */
        public static Map<String, String> getMobMap(io.lumine.xikage.mythicmobs.mobs.ActiveMob mob) {
            Map<String, String> map = new HashMap<>();
            try {
                map.put("mob_level", String.valueOf(mob.getClass().getMethod("getLevel").invoke(mob)));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot read MythicMobs 4 level", exception);
            }
            String displayName;
            try {
                displayName = (String) mob.getClass().getMethod("getDisplayName").invoke(mob);
            } catch (NoSuchMethodException ignored) {
                displayName = mob.getEntity().getBukkitEntity().getCustomName();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot read MythicMobs 4 display name", exception);
            }
            map.put("mob_name_display", displayName == null ? mob.getType().getInternalName() : displayName);
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

        /** 与 MM 4 共用出生属性来源和调度，不在回调之外持有/读取出生事件。 */
        @EventHandler(priority = EventPriority.MONITOR)
        void on(io.lumine.mythic.bukkit.events.MythicMobSpawnEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) return;
            LivingEntity entity = (LivingEntity) event.getEntity();
            String mobType = event.getMobType().getInternalName();
            try {
                io.lumine.mythic.api.config.MythicConfig config = event.getMobType().getConfig();
                List<String> attributes = MobSpawnAttributes.read(config::isSet, config::getStringList);
                List<String> equipment = new java.util.ArrayList<>(config.getStringList("SX-Equipment"));
                MobSpawnAttributes.spawn(entity, mobType, MobSpawnAttributes.level(event), attributes,
                        variables -> spawnHandler.spawn(mobType, entity.getEquipment(), variables, equipment));
            } catch (RuntimeException | LinkageError exception) {
                MobSpawnAttributes.warn(mobType, exception.toString());
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
