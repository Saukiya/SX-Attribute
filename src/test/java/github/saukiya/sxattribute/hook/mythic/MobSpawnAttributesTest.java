package github.saukiya.sxattribute.hook.mythic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** 验证出生配置、旧版等级描述符与公式入口；无需构造正在运行的 MM 怪物。 */
public class MobSpawnAttributesTest {
    /** 规范节点可用空列表覆盖历史别名，避免旧配置偷偷重新启用属性。 */
    @Test
    public void canonicalEmptyNodeOverridesAlias() {
        Map<String, List<String>> config = new LinkedHashMap<>();
        config.put("SXAttribute", Collections.emptyList());
        config.put("SX-Attribute", Arrays.asList("攻击力=100"));
        assertTrue(MobSpawnAttributes.read(config::containsKey, config::get).isEmpty());
        config.remove("SXAttribute");
        assertEquals(Arrays.asList("攻击力=100"), MobSpawnAttributes.read(config::containsKey, config::get));
    }

    /** 区域调度不能持有 MM 的可变配置列表，重载期间已经提交的出生沿用自己的副本。 */
    @Test
    public void configurationIsCopiedBeforeScheduling() {
        List<String> mutable = new ArrayList<>(Arrays.asList("攻击力=10"));
        List<String> snapshot = MobSpawnAttributes.read("sxattribute"::equals, key -> mutable);
        mutable.clear();
        assertEquals(Arrays.asList("攻击力=10"), snapshot);
    }

    /** MM 4.1 和后期版本虽然方法描述符不同，等级均保持 Number 精度。 */
    @Test
    public void readsBothHistoricalLevelReturnTypes() {
        assertEquals(7D, MobSpawnAttributes.level(new IntegerSpawn()), 0D);
        assertEquals(7.5D, MobSpawnAttributes.level(new DecimalSpawn()), 0D);
    }

    /** 出生别名先展开，再交给原生计算；两个 Lore 行复用传入的同一个引擎上下文。 */
    @Test
    public void resolvesSpawnAliasesInsideSxExpressions() {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("mob_level", "7.5");
        List<String> calls = new ArrayList<>();
        SkillExpressions expressions = new SkillExpressions(text -> {
            calls.add(text);
            return text.replace("<c:7.5*10>", "75").replace("<l:mob_level>", "7.5");
        });
        assertEquals(Arrays.asList("攻击力: 75", "防御力: 7.5"), MobSpawnAttributes.render(
                Arrays.asList("攻击力=<c:<mob.level>*10>", "防御力: <l:mob_level>"), expressions, variables));
        assertEquals(Arrays.asList("<c:7.5*10>", "防御力: <l:mob_level>"), calls);
    }

    /** 未开启公式引擎的服务器仍可配置固定属性及完整范围 Lore。 */
    @Test
    public void literalSpawnAttributesDoNotRequireFormulaEngine() {
        assertEquals(Arrays.asList("攻击力: 10 - 20", "防御力: 5"), MobSpawnAttributes.render(
                Arrays.asList("攻击力: 10 - 20", "防御力=5"), new SkillExpressions(null), Collections.emptyMap()));
    }

    /** 出生没有技能目标，不能把 target 变量静默解释为怪物自己。 */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingSpawnTargetContext() {
        MobSpawnAttributes.render(Arrays.asList("攻击力=<target.health>"),
                new SkillExpressions(text -> text), Collections.emptyMap());
    }

    /** 复现旧 MM 的 int 返回签名，不能用包装类型掩盖实际的描述符差异。 */
    public static final class IntegerSpawn {
        /** 旧版出生事件的等级协议。 */
        public int getMobLevel() { return 7; }
    }

    /** 复现新版 MM 的 double 返回签名。 */
    public static final class DecimalSpawn {
        /** 小数等级不得被出生属性解析器截断。 */
        public double getMobLevel() { return 7.5D; }
    }
}
