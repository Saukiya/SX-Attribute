package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/** 用 Bukkit 接口替身重现原生 damage 的同步事件栈，验证取消/嵌套/异常退出不会泄露上下文。 */
public class SkillDamageContextTest {
    /** 同一实体对的第二个事件不是首次命中，不能借用攻击快照。 */
    @Test
    public void capturesOnlyOneEventAndHonorsCancellation() {
        LivingEntity caster = entity(new AtomicInteger(), ignored -> { });
        AtomicInteger immunity = new AtomicInteger(15);
        LivingEntity[] target = new LivingEntity[1];
        SkillDamageContext[] context = new SkillDamageContext[1];
        EntityDamageByEntityEvent[] captured = new EntityDamageByEntityEvent[1];
        target[0] = entity(immunity, ignored -> {
            assertEquals(0, immunity.get());
            captured[0] = event(caster, target[0]);
            SkillDamageContext.capture(captured[0]);
            assertSame(context[0], SkillDamageContext.find(captured[0]));
            EntityDamageByEntityEvent nested = event(caster, target[0]);
            SkillDamageContext.capture(nested);
            assertNull(SkillDamageContext.find(nested));
            captured[0].setCancelled(true);
        });
        context[0] = context(caster, target[0]);
        assertFalse(context[0].damage(100D, true));
        assertEquals(15, immunity.get());
        assertNull(SkillDamageContext.find(captured[0]));
    }

    /** 两层技能结束时必须恢复外层事件身份，finally 最终移除 ThreadLocal。 */
    @Test
    public void restoresOuterCastAfterNestedSkill() {
        LivingEntity caster = entity(new AtomicInteger(), ignored -> { });
        LivingEntity[] target = new LivingEntity[1];
        AtomicInteger calls = new AtomicInteger();
        EntityDamageByEntityEvent[] outer = new EntityDamageByEntityEvent[1];
        target[0] = entity(new AtomicInteger(), ignored -> {
            EntityDamageByEntityEvent hit = event(caster, target[0]);
            SkillDamageContext.capture(hit);
            SkillDamageContext active = SkillDamageContext.find(hit);
            assertNotNull(active);
            if (calls.incrementAndGet() == 1) {
                outer[0] = hit;
                assertTrue(context(caster, target[0]).damage(10D, false));
                assertSame(active, SkillDamageContext.find(hit));
            } else assertEquals(2, active.getDepth());
        });
        assertTrue(context(caster, target[0]).damage(100D, false));
        assertEquals(2, calls.get());
        assertNull(SkillDamageContext.find(outer[0]));
    }

    /** 服务端伤害处理抛异常时也恢复受伤间隔，下一次施法不应继承错误的递归深度。 */
    @Test
    public void restoresStateAfterFailure() {
        AtomicInteger immunity = new AtomicInteger(12);
        LivingEntity caster = entity(new AtomicInteger(), ignored -> { });
        LivingEntity target = entity(immunity, ignored -> { throw new IllegalStateException("server damage failure"); });
        try {
            context(caster, target).damage(10D, true);
            fail("Expected damage failure");
        } catch (IllegalStateException expected) {
            assertEquals(12, immunity.get());
            assertEquals(1, context(caster, target).getDepth());
        }
    }

    /** 同步触发器递归只允许 8 次原生伤害，第 9 层拒绝后外层仍能正常返回。 */
    @Test
    public void boundsRecursiveDamageSkills() {
        LivingEntity caster = entity(new AtomicInteger(), ignored -> { });
        LivingEntity[] target = new LivingEntity[1];
        AtomicInteger calls = new AtomicInteger();
        target[0] = entity(new AtomicInteger(), ignored -> {
            SkillDamageContext.capture(event(caster, target[0]));
            int count = calls.incrementAndGet();
            assertEquals(count < 8, context(caster, target[0]).damage(10D, false));
        });
        assertTrue(context(caster, target[0]).damage(10D, false));
        assertEquals(8, calls.get());
        assertEquals(1, context(caster, target[0]).getDepth());
    }

    private SkillDamageContext context(LivingEntity caster, LivingEntity target) {
        return new SkillDamageContext(caster, target, new SXAttributeData(), 1D, "test", Collections.emptyList());
    }

    private EntityDamageByEntityEvent event(LivingEntity caster, LivingEntity target) {
        try {
            // 1.20 的旧构造器会隐式访问服务器 DamageType 注册表；测试显式提供替身，不启动服务器。
            Class<?> sourceType = Class.forName("org.bukkit.damage.DamageSource");
            Object source = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{sourceType},
                    (proxy, method, args) -> { throw new UnsupportedOperationException(method.getName()); });
            return EntityDamageByEntityEvent.class.getConstructor(org.bukkit.entity.Entity.class,
                    org.bukkit.entity.Entity.class, EntityDamageEvent.DamageCause.class, sourceType, double.class)
                    .newInstance(caster, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, source, 10D);
        } catch (ClassNotFoundException ignored) {
            return new EntityDamageByEntityEvent(caster, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Cannot create test damage event", exception);
        }
    }

    /** 只模拟上下文依赖的 Bukkit 行为，任何未预期调用都会令测试失败。 */
    private LivingEntity entity(AtomicInteger immunity, Consumer<Object[]> damage) {
        return (LivingEntity) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{LivingEntity.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "equals": return proxy == args[0];
                        case "hashCode": return System.identityHashCode(proxy);
                        case "toString": return "skill-test-entity";
                        case "getNoDamageTicks": return immunity.get();
                        case "setNoDamageTicks": immunity.set((Integer) args[0]); return null;
                        case "isValid": return true;
                        case "damage": damage.accept(args); return null;
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                });
    }
}
