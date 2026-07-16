package github.saukiya.sxattribute.event;

import org.bukkit.Bukkit;

/**
 * 自定义事件线程标记的唯一事实源。
 * <p>
 * Bukkit 要求事件的异步标记与实际调用线程严格一致；历史 API 暴露的 {@code isAsync} 参数描述的是
 * 调用意图，容易被延迟同步任务误传，因此所有事件必须在构造时重新查询当前线程。
 */
final class EventThreadContext {

    private EventThreadContext() {
    }

    static boolean isAsynchronous() {
        return isAsynchronous(Bukkit.isPrimaryThread());
    }

    static boolean isAsynchronous(boolean primaryThread) {
        return !primaryThread;
    }
}
