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

    /** 全局线程不拥有任何实体；普通 Bukkit 则沿用主线程约束。 */
    public static boolean owns(Entity entity) {
        return FOLIA ? Bukkit.isOwnedByCurrentRegion(entity) : Bukkit.isPrimaryThread();
    }

    /**
     * 跟随实体迁移调度，并在实体已退役时反馈失败。retired 属于服务端退役关键流程，
     * 此处约定它只能清理插件状态，不能继续读取已移除实体或发布 Bukkit 事件。
     */
    public static boolean executeEntity(Entity entity, Plugin plugin, Runnable task, Runnable retired) {
        if (FOLIA) return entity.getScheduler().execute(plugin, task, retired, 1L);
        if (Bukkit.isPrimaryThread()) task.run();
        else Bukkit.getScheduler().runTask(plugin, task);
        return true;
    }

    /**
     * 在全局调度器上执行一次任务。调用方沿用 Bukkit 的约定，可用 0 表示尽快执行；
     * Folia 的延迟任务至少要求 1 tick，因此仅在 Folia 分支统一修正该边界。
     */
    public static Object runSync(Plugin plugin, Runnable task, long delay) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (Consumer) ignored -> task.run(), foliaDelay(delay));
    }

    /**
     * 在全局调度器上周期执行任务，保留 Bukkit 对首次 0 tick 延迟的使用方式。
     */
    public static Object runTimer(Plugin plugin, Runnable task, long delay, long period) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (Consumer) ignored -> task.run(), foliaDelay(delay), period);
    }

    /**
     * 在实体所属区域执行一次任务，避免在 Folia 上跨区域访问实体。
     */
    public static Object runEntity(Entity entity, Plugin plugin, Runnable task, long delay) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return entity.getScheduler().runDelayed(plugin, (Consumer) ignored -> task.run(), null, foliaDelay(delay));
    }

    /**
     * 在实体所属区域周期执行任务，并允许调用方继续使用 Bukkit 的首次 0 tick 延迟约定。
     */
    public static Object runEntityTimer(Entity entity, Plugin plugin, Runnable task, long delay, long period) {
        if (!FOLIA) return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return entity.getScheduler().runAtFixedRate(plugin, (Consumer) ignored -> task.run(), null, foliaDelay(delay), period);
    }

    public static void cancel(Object task) {
        if (task instanceof org.bukkit.scheduler.BukkitTask) {
            ((org.bukkit.scheduler.BukkitTask) task).cancel();
        } else if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask) {
            ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
        }
    }

    /**
     * Folia 拒绝小于 1 tick 的延迟；归一化只发生在 Folia 分支，不改变 Bukkit 调度行为。
     */
    private static long foliaDelay(long delay) {
        return Math.max(1L, delay);
    }

    private static boolean hasFoliaScheduler() {
        try {
            // 服务端名称可被分支实现或启动器改写；以 Folia 核心类为能力边界，避免误走线程不安全的 BukkitScheduler。
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false,
                    FoliaScheduler.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
