package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** 覆盖技能列表协议和跨目标数据隔离，不依赖正在运行的 MythicMobs。 */
public class SkillAttributesTest {
    /** 老 MM 可用 |，后期 MM 可用嵌套花括号；函数中的逗号不能拆成另一个属性。 */
    @Test
    public void parsesPortableAndNestedLists() {
        assertEquals(Arrays.asList("攻击力=100", "暴击几率=50"), SkillAttributes.split("攻击力=100|暴击几率=50"));
        assertEquals(Arrays.asList("攻击力=max(10,20)", "暴击几率=50", "攻击力: 10 - 20"),
                SkillAttributes.split("{攻击力=max(10,20);暴击几率=50,攻击力: 10 - 20}"));
        assertEquals(Collections.emptyList(), SkillAttributes.split("{}"));
        assertEquals(Collections.emptyList(), SkillAttributes.split(null));
    }

    /** 不平衡的分隔符必须作为配置错误反馈，不能施加一部分属性。 */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnbalancedList() {
        SkillAttributes.split("攻击力=(100;暴击几率=20");
    }

    /** 负数动态字段不能被 MAX 聚合归零，且后续目标不能读到前一个目标的修改。 */
    @Test
    public void copiesDynamicFieldsWithoutAggregationOrAliasing() {
        SXAttributeData original = new SXAttributeData();
        original.addDynamicValue("Curse", "value", -12D);
        SXAttributeData copy = SkillAttributes.copy(original, 0.5D);
        assertEquals(-6D, copy.getDynamicValue("Curse", "value"), 0D);
        copy.getDynamicValues().get("Curse").put("value", 99D);
        assertEquals(-12D, original.getDynamicValue("Curse", "value"), 0D);
    }

    /** 极端倍率不得把非有限数值写入 Bukkit 或缓存来源。 */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsOverflowedSnapshot() {
        SXAttributeData original = new SXAttributeData();
        original.addDynamicValue("Power", "value", Double.MAX_VALUE);
        SkillAttributes.copy(original, 2D);
    }

    /** 命名空间和三种伤害别名必须稳定，安装 AP 扩展时不能争抢其 mechanic 名称。 */
    @Test
    public void keepsAliasesAndForeignMechanicsSeparate() {
        assertEquals(AttributeSkill.Kind.DAMAGE, AttributeSkill.resolve("DamageSX"));
        assertEquals(AttributeSkill.Kind.BASE_DAMAGE, AttributeSkill.resolve("SXBaseDamage"));
        assertEquals(AttributeSkill.Kind.PERCENT_DAMAGE, AttributeSkill.resolve("PercentDamageSX"));
        assertNull(AttributeSkill.resolve("DamageAP"));
        assertNull(AttributeSkill.resolve("damage"));
        assertTrue(SkillAttributes.copy(null, 1D).getDynamicValues().isEmpty());
    }

    /** 复现 MM 4.1 的第二个等号截断问题，并确保选择器参数不会覆盖技能参数。 */
    @Test
    public void readsFullValuesFromLegacyMechanicLine() {
        java.util.Map<String, String> parameters = SkillParameters.parse(
                "SXDamage{al=攻击力=100|暴击几率=50;a=20} @PlayersInRadius{a=999;r=5}");
        assertEquals("攻击力=100|暴击几率=50", parameters.get("al"));
        assertEquals("20", parameters.get("a"));
        assertEquals(2, parameters.size());
        assertEquals("{攻击力=100;暴击几率=50}",
                SkillParameters.parse("SXDamage{al={攻击力=100;暴击几率=50};pi=true}").get("al"));
        assertTrue(SkillParameters.parse("SXAttrUpdate @PlayersInRadius{r=5}").isEmpty());
    }
}
