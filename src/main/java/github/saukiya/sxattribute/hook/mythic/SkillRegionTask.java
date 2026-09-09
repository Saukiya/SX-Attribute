package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.FoliaScheduler;
import org.bukkit.entity.LivingEntity;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/** 非阻塞的施法接力；实体消失/插件停用会终结任务，每次施法最多提交一个后继阶段。 */
public final class SkillRegionTask {
    /** 调度边界可替换，以验证区域迁移/退役竞态；生产实现只调用服务端实体调度器。 */
    interface RegionAccess {
        /** 判断当前线程能否访问实体状态，不能仅凭相同世界或线程名判断。 */
        boolean owns(LivingEntity entity);
        /** 提交后 action/retired 至多执行其一；返回 false 表示二者都不会执行。 */
        boolean schedule(LivingEntity entity, Runnable action, Runnable retired);
        /** 插件停用后，尚未开始的阶段不得继续访问来源与属性服务。 */
        boolean enabled();
    }

    private static final RegionAccess LIVE = new RegionAccess() {
        @Override
        public boolean owns(LivingEntity entity) {
            return FoliaScheduler.owns(entity);
        }

        @Override
        public boolean schedule(LivingEntity entity, Runnable action, Runnable retired) {
            return FoliaScheduler.executeEntity(entity, SXAttribute.getInst(), action, retired);
        }

        @Override
        public boolean enabled() {
            return SXAttribute.getInst().isEnabled();
        }
    };
    /** 防止范围技能或异常配置无限持有实体引用；这是全插件在途施法数量上限。 */
    private static final Semaphore CAPACITY = new Semaphore(1024);
    private static final Set<SkillRegionTask> ACTIVE = ConcurrentHashMap.newKeySet();
    private final CompletableFuture<Boolean> completion = new CompletableFuture<>();
    private final Consumer<String> failure;
    private final RegionAccess regions;

    private SkillRegionTask(Consumer<String> failure, RegionAccess regions) {
        this.failure = failure;
        this.regions = regions;
        ACTIVE.add(this);
        completion.whenComplete((result, error) -> {
            ACTIVE.remove(this);
            CAPACITY.release();
        });
    }

    /** 返回 null 表示过载；调用方必须向 MM 反馈拒绝，不能无限排队。 */
    static SkillRegionTask create(Consumer<String> failure) {
        return create(failure, LIVE);
    }

    /** 测试注入不会绕过容量/完成协议，保证竞态用例走真实接力代码。 */
    static SkillRegionTask create(Consumer<String> failure, RegionAccess regions) {
        return CAPACITY.tryAcquire() ? new SkillRegionTask(failure, regions) : null;
    }

    /** 已在正确区域时直接执行，其他情况跟随实体调度；所有阶段都重新检查存活状态。 */
    void at(LivingEntity entity, Runnable action) {
        if (completion.isDone()) return;
        Runnable checked = () -> {
            if (completion.isDone()) return;
            try {
                if (!regions.owns(entity)) throw new IllegalStateException("Entity scheduler did not grant ownership");
                if (!regions.enabled() || !entity.isValid() || entity.isDead()) {
                    fail("entity retired or died before skill execution");
                    return;
                }
                action.run();
            } catch (RuntimeException | LinkageError exception) {
                fail(exception.toString());
            }
        };
        try {
            if (regions.owns(entity)) checked.run();
            else if (!regions.schedule(entity, checked,
                    () -> fail("entity retired before skill execution"))) fail("entity scheduler rejected skill");
        } catch (RuntimeException | LinkageError exception) {
            fail(exception.toString());
        }
    }

    /** 同步完成时反馈真实结果；尚在跨区接力时仅表示已接受调度，不阻塞 tick 等待。 */
    boolean accepted() {
        return completion.getNow(true);
    }

    /** 调用一次结束，退役回调和执行异常竞争时也只释放一次容量。 */
    void finish(boolean result) {
        if (!result) fail("skill execution returned false");
        else completion.complete(true);
    }

    /** 在实体写入前拒绝不具备安全执行条件的机制，保留具体原因。 */
    void reject(String reason) {
        fail(reason);
    }

    private void fail(String reason) {
        if (completion.complete(false)) failure.accept(reason);
    }

    /** 停用时释放所有在途施法，已排入服务端的回调随后只会空返回。 */
    public static void shutdown() {
        for (SkillRegionTask task : ACTIVE) task.completion.complete(false);
    }
}
