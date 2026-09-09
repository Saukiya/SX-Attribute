package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.data.RandomStringManager;
import github.saukiya.sxattribute.util.FormulaUtil;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/** 每次施法、每个目标独享 SX 变量空间，所有数值与 Lore 共用锁定随机结果。 */
final class SkillExpressions {
    private static final Pattern TOKEN_START = Pattern.compile("<[A-Za-z][A-Za-z0-9_-]*[:.]");
    private static final Pattern PAPI_TOKEN = Pattern.compile("%[A-Za-z][^%\\s]*%");
    private final UnaryOperator<String> engine;

    /** 延迟进入依赖类，关闭公式引擎时字面量技能仍可运行。 */
    static SkillExpressions create(Player player, Map<String, Double> variables) {
        return create(player, variables, Collections.emptyMap());
    }

    /** 出生上下文同时包含等级数值与名称/UUID 文本，二者共享原生锁定变量协议。 */
    static SkillExpressions create(Player player, Map<String, Double> variables, Map<String, String> textVariables) {
        return new SkillExpressions(FormulaUtil.isUsable() ? NativeEngine.create(player, variables, textVariables) : null);
    }

    /** 可注入解析器以独立验证求值顺序；生产环境始终使用 SX 的 Handler。 */
    SkillExpressions(UnaryOperator<String> engine) {
        this.engine = engine;
    }

    /** 先展开原生表达式，再计算裸数学式；不能将随机范围或字符串组直接塞进计算器。 */
    double number(String input) {
        String expanded = text(input).trim();
        try {
            return SkillAttributes.finite(Double.parseDouble(expanded));
        } catch (NumberFormatException ignored) {
            if (engine == null) throw new IllegalArgumentException("SX formula engine is required: " + input);
            if (expanded.contains("<") || expanded.contains(">")) {
                throw new IllegalArgumentException("Unresolved numeric expression: " + input);
            }
            return SkillAttributes.finite(Double.parseDouble(engine.apply("<c:" + expanded + ">").trim()));
        }
    }

    /** 完整 Lore 同样展开 SX 标记，范围与百分号仍交给既有属性识别器处理。 */
    List<String> lore(String input) {
        List<String> result = new ArrayList<>();
        for (String line : SkillAttributes.split(input)) {
            int equals = line.indexOf('=');
            int colon = line.indexOf(':');
            // 只将识别名后的首个等号视为简写，不能吞掉冒号 Lore 内条件表达式的比较符。
            if (equals >= 0 && (colon < 0 || equals < colon)) {
                if (equals == 0) throw new IllegalArgumentException("Attribute name is empty");
                line = line.substring(0, equals).trim() + ": "
                        + BigDecimal.valueOf(number(line.substring(equals + 1))).stripTrailingZeros().toPlainString();
            } else {
                line = text(line);
            }
            // 随机词条可返回多行 Lore；遵循 SX 原生的删除行标记，不把它写入持久化来源。
            for (String rendered : line.split("\\r?\\n")) {
                if (!rendered.trim().isEmpty() && !rendered.contains("<DeleteLore>")) result.add(rendered);
            }
        }
        return result;
    }

    private String text(String input) {
        if (engine != null) return engine.apply(input);
        if (TOKEN_START.matcher(input).find() || PAPI_TOKEN.matcher(input).find()) {
            throw new IllegalArgumentException("SX formula engine is required: " + input);
        }
        return input;
    }

    /** 只把 MM 标记交给 MM，保留 SX 外层表达式，并允许其中嵌入 MM 的等级/目标变量。 */
    static String mythic(String input, UnaryOperator<String> placeholders) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            int end = tokenEnd(input, index);
            if (end < 0) {
                result.append(input.charAt(index));
                continue;
            }
            String body = input.substring(index + 1, end);
            int colon = body.indexOf(':');
            int dot = body.indexOf('.');
            String token = "<" + mythic(body, placeholders) + ">";
            result.append(colon >= 0 && (dot < 0 || colon < dot) ? token : placeholders.apply(token));
            index = end;
        }
        return result.toString();
    }

    /** 定位嵌套占位符，列表/机制分隔符在标记内部没有结构意义；普通小于号不是标记。 */
    static int tokenEnd(String input, int start) {
        if (input.charAt(start) != '<' || !TOKEN_START.matcher(input).region(start, input.length()).lookingAt()) return -1;
        for (int index = start + 1; index < input.length(); index++) {
            if (input.charAt(index) == '>') return index;
            int nested = tokenEnd(input, index);
            if (nested >= 0) index = nested;
        }
        throw new IllegalArgumentException("Unclosed skill expression: " + input);
    }

    /** 单独隔离 SX-Item 链接；注入两张表以兼容旧引擎仅从 lockMap 读取无 # 的变量。 */
    private static final class NativeEngine {
        private static UnaryOperator<String> create(Player player, Map<String, Double> variables, Map<String, String> textVariables) {
            Map<String, String> locks = new HashMap<>();
            variables.forEach((key, value) -> locks.put(key, String.valueOf(value)));
            locks.putAll(textVariables);
            RandomStringManager.Handler handler = new RandomStringManager.Handler(player, locks);
            handler.getOtherMap().putAll(locks);
            return handler::replace;
        }
    }
}
