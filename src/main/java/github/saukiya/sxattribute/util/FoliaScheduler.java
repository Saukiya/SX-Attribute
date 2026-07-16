package github.saukiya.sxattribute.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * 统一封装 Bukkit 与 Folia 调度器。Folia API 直接参与编译，运行时仅在检测到 Folia
 * 时调用区域调度器；普通 Spigot/Paper 仍走 BukkitScheduler，保持同一插件包兼容三类服务端。
 */
public final class FoliaScheduler {

    private static final boolean FOLIA = hasFoliaScheduler();

    private FoliaScheduler() {
    }

    /** @return 是否运行在提供 Folia 区域调度器的服务端。 */
    public static boolean isFolia() {
        return FOLIA;
    }

    public static Object runSync(Plugin plugin, Runnable task, long delay) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (Consumer) ignored -> task.run(), delay);
    }

    public static Object runTimer(Plugin plugin, Runnable task, long delay, long period) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (Consumer) ignored -> task.run(), delay, period);
    }

    public static Object runEntity(Entity entity, Plugin plugin, Runnable task, long delay) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return entity.getScheduler().runDelayed(plugin, (Consumer) ignored -> task.run(), null, delay);
    }

    public static Object runEntityTimer(Entity entity, Plugin plugin, Runnable task, long delay, long period) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return entity.getScheduler().runAtFixedRate(plugin, (Consumer) ignored -> task.run(), null, delay, period);
    }

    public static void cancel(Object task) {
        if (task instanceof org.bukkit.scheduler.BukkitTask) {
            ((org.bukkit.scheduler.BukkitTask) task).cancel();
        } else if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask) {
            ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
        }
    }

    private static boolean hasFoliaScheduler() {
        return Bukkit.getName().equalsIgnoreCase("Folia")
                || Bukkit.getServer().getName().equalsIgnoreCase("Folia");
    }
}
