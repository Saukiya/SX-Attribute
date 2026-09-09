package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** 技能使用独立数值快照；不能用聚合规则复制，否则 MAX/LAST 字段可能在多目标施法中改变。 */
public final class SkillAttributes {
    private SkillAttributes() { }

    /** 同时复制旧数组和动态字段，倍率不修改装备、来源或另一个目标的技能数据。 */
    public static SXAttributeData copy(SXAttributeData original, double multiplier) {
        SXAttributeData result = new SXAttributeData();
        if (original == null) return result;
        for (int i = 0; i < original.getValues().length; i++) {
            for (int j = 0; j < original.getValues()[i].length; j++) {
                result.getValues()[i][j] = finite(original.getValues()[i][j] * multiplier);
            }
        }
        original.getDynamicValues().forEach((id, fields) -> {
            LinkedHashMap<String, Double> values = new LinkedHashMap<>();
            fields.forEach((field, value) -> values.put(field, finite(value * multiplier)));
            result.getDynamicValues().put(id, values);
        });
        return result;
    }

    /** NaN、无穷值不能进入 Bukkit 伤害、生命或属性源。 */
    public static double finite(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Skill value must be finite");
        return value;
    }

    /**
     * 分割属性列表，保留 SX/MM 标记、公式函数参数与嵌套花括号；支持 ;、, 和 | 分隔。
     * 旧属性范围可直接写成 Lore（攻击力: 10 - 20），避免把减法公式误认为范围。
     */
    public static List<String> split(String input) {
        String text = input == null ? "" : input.trim();
        if (text.startsWith("{") && text.endsWith("}")) text = text.substring(1, text.length() - 1);
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int index = 0; index < text.length(); index++) {
            int tokenEnd = SkillExpressions.tokenEnd(text, index);
            if (tokenEnd >= 0) {
                index = tokenEnd;
                continue;
            }
            char c = text.charAt(index);
            if (c == '(' || c == '{' || c == '[') depth++;
            if (c == ')' || c == '}' || c == ']') depth--;
            if (depth < 0) throw new IllegalArgumentException("Unbalanced attribute list: " + input);
            if (depth == 0 && (c == ';' || c == ',' || c == '|')) {
                addPart(result, text.substring(start, index));
                start = index + 1;
            }
        }
        if (depth != 0) throw new IllegalArgumentException("Unbalanced attribute list: " + input);
        addPart(result, text.substring(start));
        return result;
    }

    private static void addPart(List<String> result, String part) {
        if (!part.trim().isEmpty()) result.add(part.trim());
    }
}
