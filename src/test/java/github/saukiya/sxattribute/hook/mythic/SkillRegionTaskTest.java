package github.saukiya.sxattribute.hook.mythic;

import org.bukkit.entity.LivingEntity;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.*;

/** 模拟独立区域线程和实体退役，跨区读取会立即报错；不以睡眠或阻塞 Future 模拟 Folia。 */
public class SkillRegionTaskTest {
    /** 即使用例断言失败也释放全局在途容量，避免污染后续用例。 */
    @After
    public void cleanup() {
        SkillRegionTask.shutdown();
    }

    /** 从无实体所有权的线程提交时不读取实体，两个阶段按各自区域执行且仅写入一次。 */
    @Test
    public void relaysWithoutCrossRegionReadsOrBlocking() {
        Regions regions = new Regions();
        LivingEntity caster = regions.entity();
        LivingEntity target = regions.entity();
        List<String> phases = new ArrayList<>();
        SkillRegionTask task = SkillRegionTask.create(reason -> fail(reason), regions);
        assertNotNull(task);
        task.at(caster, () -> {
            phases.add("capture");
            task.at(target, () -> {
                phases.add("apply");
                task.finish(true);
            });
        });
        assertTrue(task.accepted());
        assertTrue(phases.isEmpty());
        regions.next();
        assertEquals(Arrays.asList("capture"), phases);
        regions.next();
        assertEquals(Arrays.asList("capture", "apply"), phases);
        assertTrue(task.accepted());
        task.at(target, () -> fail("finished task executed again"));
        assertTrue(regions.queue.isEmpty());
    }

    /** 目标在调度后退役时不写入，迟到/重复回调不能再次完成或二次释放容量。 */
    @Test
    public void retirementStopsLateCallbacks() {
        Regions regions = new Regions();
        LivingEntity target = regions.entity();
        List<String> errors = new ArrayList<>();
        SkillRegionTask task = SkillRegionTask.create(errors::add, regions);
        task.at(target, () -> fail("retired entity was modified"));
        Entry entry = regions.queue.remove();
        entry.retired.run();
        entry.retired.run();
        entry.action.run();
        assertFalse(task.accepted());
        assertEquals(1, errors.size());
    }

    /** 服务端拒绝提交时同步反馈失败，后续阶段不可继续接力。 */
    @Test
    public void rejectedSubmissionReportsFailure() {
        Regions regions = new Regions();
        regions.reject = true;
        List<String> errors = new ArrayList<>();
        SkillRegionTask task = SkillRegionTask.create(errors::add, regions);
        task.at(regions.entity(), () -> fail("rejected task ran"));
        assertFalse(task.accepted());
        assertEquals(1, errors.size());
    }

    /** 已拥有实体时直接执行并返回真实 false，不将失败误报为已排队成功。 */
    @Test
    public void synchronousResultIsPreserved() {
        Regions regions = new Regions();
        regions.owner = regions.entity();
        SkillRegionTask task = SkillRegionTask.create(reason -> { }, regions);
        task.at(regions.owner, () -> task.finish(false));
        assertFalse(task.accepted());
        assertTrue(regions.queue.isEmpty());
    }

    /** 停用清理后，服务端仍持有的排队回调不能进入实体 API 或来源服务。 */
    @Test
    public void shutdownMakesPendingCallbacksInert() {
        Regions regions = new Regions();
        SkillRegionTask task = SkillRegionTask.create(reason -> fail(reason), regions);
        task.at(regions.entity(), () -> fail("disabled skill ran"));
        SkillRegionTask.shutdown();
        regions.queue.remove().action.run();
        assertFalse(task.accepted());
    }

    /** 校验所有权必须发生在 isValid/isDead 之前，调度器错误不能变成一次越线程访问。 */
    @Test
    public void wrongOwnerFailsBeforeReadingEntity() {
        Regions regions = new Regions();
        List<String> errors = new ArrayList<>();
        SkillRegionTask task = SkillRegionTask.create(errors::add, regions);
        task.at(regions.entity(), () -> fail("wrong-region task ran"));
        regions.queue.remove().action.run();
        assertFalse(task.accepted());
        assertTrue(errors.get(0).contains("ownership"));
    }

    /** 任意阶段异常会结束接力，不留下永久占用在途容量的施法。 */
    @Test
    public void executionExceptionFinishesTask() {
        Regions regions = new Regions();
        List<String> errors = new ArrayList<>();
        SkillRegionTask task = SkillRegionTask.create(errors::add, regions);
        task.at(regions.entity(), () -> { throw new IllegalArgumentException("bad formula"); });
        regions.next();
        assertFalse(task.accepted());
        assertTrue(errors.get(0).contains("bad formula"));
    }

    /** 全局过载上限必须有效，重复终结不能把同一容量释放两次。 */
    @Test
    public void capacityIsBoundedAndReleasedExactlyOnce() {
        Regions regions = new Regions();
        List<SkillRegionTask> tasks = new ArrayList<>();
        for (int index = 0; index < 1024; index++) {
            SkillRegionTask task = SkillRegionTask.create(reason -> { }, regions);
            assertNotNull(task);
            tasks.add(task);
        }
        assertNull(SkillRegionTask.create(reason -> { }, regions));
        tasks.get(0).reject("cancelled");
        tasks.get(0).reject("cancelled again");
        assertNotNull(SkillRegionTask.create(reason -> { }, regions));
        assertNull(SkillRegionTask.create(reason -> { }, regions));
    }

    /** 每个队列项携带实体身份，执行时取得当前实体所有权，模拟迁移后的实体调度器。 */
    private static final class Entry {
        final LivingEntity entity;
        final Runnable action;
        final Runnable retired;

        Entry(LivingEntity entity, Runnable action, Runnable retired) {
            this.entity = entity;
            this.action = action;
            this.retired = retired;
        }
    }

    /** 未持有对应 owner 的任何存活状态读取都会失败，避免测试替身容忍生产中的非法读取。 */
    private static final class Regions implements SkillRegionTask.RegionAccess {
        final Deque<Entry> queue = new ArrayDeque<>();
        LivingEntity owner;
        boolean reject;

        LivingEntity entity() {
            return (LivingEntity) Proxy.newProxyInstance(LivingEntity.class.getClassLoader(),
                    new Class<?>[]{LivingEntity.class}, (proxy, method, args) -> {
                        if (method.getName().equals("isValid") || method.getName().equals("isDead")) {
                            assertSame("cross-region entity read", owner, proxy);
                            return method.getName().equals("isValid");
                        }
                        throw new AssertionError("Unexpected entity access: " + method.getName());
                    });
        }

        void next() {
            Entry entry = queue.remove();
            owner = entry.entity;
            try {
                entry.action.run();
            } finally {
                owner = null;
            }
        }

        @Override
        public boolean owns(LivingEntity entity) { return owner == entity; }

        @Override
        public boolean schedule(LivingEntity entity, Runnable action, Runnable retired) {
            if (reject) return false;
            queue.add(new Entry(entity, action, retired));
            return true;
        }

        @Override
        public boolean enabled() { return true; }
    }
}
