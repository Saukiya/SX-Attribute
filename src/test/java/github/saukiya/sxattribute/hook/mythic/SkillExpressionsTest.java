package github.saukiya.sxattribute.hook.mythic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** 检验引擎边界和解析协议；解析器替身只验证接线，不替代 SX-Item 实服随机/锁定验收。 */
public class SkillExpressionsTest {
    /** 嵌套 MM 值先解析，但不能让不同代 MM 吞掉 SX 的 c/l/r 等标记。 */
    @Test
    public void routesOnlyMythicTokensToMythic() {
        List<String> calls = new ArrayList<>();
        String result = SkillExpressions.mythic("<c:<caster.level>*<l:普通基数>+<r:1_5>>", token -> {
            calls.add(token);
            return "10";
        });
        assertEquals("<c:10*<l:普通基数>+<r:1_5>>", result);
        assertEquals(Arrays.asList("<caster.level>"), calls);
    }

    /** 随机值先展开一次，裸数学式仅使用该结果，防止计算阶段再次抽样。 */
    @Test
    public void expandsBeforeCalculatingBareArithmetic() {
        List<String> calls = new ArrayList<>();
        SkillExpressions expressions = new SkillExpressions(text -> {
            calls.add(text);
            return text.equals("<r:1_5>+2") ? "4+2" : "6";
        });
        assertEquals(6D, expressions.number("<r:1_5>+2"), 0D);
        assertEquals(Arrays.asList("<r:1_5>+2", "<c:4+2>"), calls);
    }

    /** 数值结果不重复送入计算器，持续时间/层数/倍率均能直接使用 SX 原生结果。 */
    @Test
    public void acceptsNativeNumericResultsWithoutRecalculation() {
        List<String> calls = new ArrayList<>();
        SkillExpressions expressions = new SkillExpressions(text -> {
            calls.add(text);
            return "1.25";
        });
        assertEquals(1.25D, expressions.number("<s:优秀基数>"), 0D);
        assertEquals(Arrays.asList("<s:优秀基数>"), calls);
    }

    /** 完整 Lore 的条件等号不属于简写分隔符；范围与百分号保留给 SX 的识别器。 */
    @Test
    public void expandsColonLoreAndMultilineGroups() {
        SkillExpressions expressions = new SkillExpressions(text -> text
                .replace("<r:10_20>", "15").replace("<r:30_40>", "35")
                .replace("<if:1==1_20_0>", "20")
                .replace("<s:组合>", "攻击力: 10\n<DeleteLore>\n防御力: 5"));
        assertEquals(Arrays.asList("攻击力: 15 - 35", "暴击几率: 20%", "攻击力: 10", "防御力: 5"),
                expressions.lore("攻击力: <r:10_20> - <r:30_40>|暴击几率: <if:1==1_20_0>%|<s:组合>"));
    }

    /** 原生表达式内部的分隔符不能改变属性数或截断机制参数，两代 MM 共享此协议。 */
    @Test
    public void preservesDelimitersInsideNestedTokens() {
        String attributes = "攻击力=<s:10,20:30;40>|防御力=<if:<b:true|false>_1_0>";
        assertEquals(Arrays.asList("攻击力=<s:10,20:30;40>", "防御力=<if:<b:true|false>_1_0>"),
                SkillAttributes.split(attributes));
        assertEquals(attributes, SkillParameters.parse("SXDamage{al=" + attributes + ";a=1} @target").get("al"));
    }

    /** 关闭 SX 表达式引擎不影响字面量技能，也不把 Lore 的百分比当作 PAPI。 */
    @Test
    public void literalsRemainUsableWithoutEngine() {
        SkillExpressions expressions = new SkillExpressions(null);
        assertEquals(12.5D, expressions.number("12.5"), 0D);
        assertEquals(Arrays.asList("攻击力: 10 - 20", "暴击几率: 50%", "防御力: 5"),
                expressions.lore("攻击力: 10 - 20|暴击几率: 50%|防御力=5"));
    }

    /** 引擎缺失不能将动态参数悄悄作为零值伤害执行。 */
    @Test(expected = IllegalArgumentException.class)
    public void expressionsRequireEngine() {
        new SkillExpressions(null).number("<r:10_20>");
    }

    /** 未解析变量必须失败，不能尝试让计算器修正残留标记。 */
    @Test(expected = IllegalArgumentException.class)
    public void unresolvedVariablesFail() {
        new SkillExpressions(text -> text).number("<l:missing>");
    }

    /** 引擎算出的无限值必须与字面量无限值一样被拒绝。 */
    @Test(expected = IllegalArgumentException.class)
    public void nativeOverflowFails() {
        new SkillExpressions(text -> "Infinity").number("<c:1e308*100>");
    }
}
