package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxitem.data.expression.ExpressionHandler;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性生效/转换公式求值 (基于 SX-Item {@link ExpressionHandler}, 软依赖)
 * <p>
 * 公式变量用 SX-Item 原生 {@code <l:xxx>} 语法, 由 {@code otherMap} 注入 (见
 * {@code LockStringExpression} 优先读 otherMap): {@code <l:value>}=词条累加值,
 * {@code <l:default>}=默认基值, {@code <l:base>}=当前基值, {@code <l:min>}/{@code <l:max>}=限幅。
 * 外层套 {@code <c: … >} 做数学运算, 并自动支持 PlaceholderAPI。
 * <p>
 * SX-Item 为 {@code compileOnly} 依赖: 运行期未安装时 {@link #AVAILABLE} 为 false,
 * {@link #eval} 直接返回 fallback (由调用方回退 Mode 预设), 全程不触碰 SX-Item 类, 避免 NoClassDefFoundError。
 *
 * @author Ray_Hughes
 */
public class FormulaUtil {

    private static final String[] COMPARISON_OPERATORS = {">=", "<=", "==", "!=", ">", "<"};

    /**
     * 运行期 SX-Item 公式引擎是否可用 (类存在性探测)
     */
    private static final boolean AVAILABLE = classPresent("github.saukiya.sxitem.data.expression.ExpressionHandler");

    /**
     * 已告警的坏公式, 避免每 tick 刷屏
     */
    private static final Set<String> WARNED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static boolean classPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * @return 公式引擎是否可用 (类存在 且 Settings.FormulaEngine 开启)
     */
    public static boolean isUsable() {
        return AVAILABLE && AttributeConfig.isFormulaEngine();
    }

    /**
     * 求值公式; 引擎不可用/公式为空/求值失败均返回 fallback
     *
     * @param player   玩家 (供 PlaceholderAPI)
     * @param formula  公式串, 形如 {@code "<c:<l:default> * (100 + <l:value>) / 100>"}
     * @param vars     变量表 (key 用于 {@code <l:key>} 引用)
     * @param fallback 兜底值
     * @return 求值结果, 失败回退 fallback
     */
    public static double eval(Player player, String formula, Map<String, Double> vars, double fallback) {
        if (formula == null || formula.isEmpty() || !isUsable()) return fallback;
        try {
            ExpressionHandler handler = new ExpressionHandler(player);
            for (Map.Entry<String, Double> e : vars.entrySet()) {
                handler.getOtherMap().put(e.getKey(), String.valueOf(e.getValue()));
            }
            String result = handler.replace(formula);
            return Double.parseDouble(result.trim());
        } catch (Throwable t) {
            if (WARNED.add(formula)) {
                SXAttribute.getInst().getLogger().warning("Formula eval failed, fallback to Mode: " + formula + " (" + t.getMessage() + ")");
            }
            return fallback;
        }
    }

    /**
     * 求值触发器条件公式。
     * <p>
     * SX-Item 的数学表达式不识别比较运算符，并会把 {@code >} 当作表达式结束符；这里先在
     * {@code <l:变量>} 标记之外解析 {@code &&}/{@code ||} 与比较符，再把两侧交回原公式引擎。
     * 这样实体类型、生命比例等条件仍使用同一变量协议，同时不会因解析失败静默跳过属性效果。
     *
     * @param player   占位符上下文玩家
     * @param formula  条件公式
     * @param vars     统一公式变量
     * @param fallback 公式不可用或非法时的结果
     * @return 条件是否成立
     */
    public static boolean evalCondition(Player player, String formula, Map<String, Double> vars, boolean fallback) {
        if (formula == null || formula.trim().isEmpty() || !isUsable()) return fallback;
        try {
            return evalBooleanExpression(player, unwrapCalculation(formula), vars);
        } catch (RuntimeException exception) {
            if (WARNED.add("condition:" + formula)) {
                SXAttribute.getInst().getLogger().warning("Condition formula eval failed, fallback to " + fallback
                        + ": " + formula + " (" + exception.getMessage() + ")");
            }
            return fallback;
        }
    }

    private static boolean evalBooleanExpression(Player player, String expression, Map<String, Double> vars) {
        int orIndex = findOutsideToken(expression, "||");
        if (orIndex >= 0) {
            return evalBooleanExpression(player, expression.substring(0, orIndex), vars)
                    || evalBooleanExpression(player, expression.substring(orIndex + 2), vars);
        }
        int andIndex = findOutsideToken(expression, "&&");
        if (andIndex >= 0) {
            return evalBooleanExpression(player, expression.substring(0, andIndex), vars)
                    && evalBooleanExpression(player, expression.substring(andIndex + 2), vars);
        }
        for (String operator : COMPARISON_OPERATORS) {
            int operatorIndex = findOutsideToken(expression, operator);
            if (operatorIndex < 0) continue;
            double left = evalOperand(player, expression.substring(0, operatorIndex), vars);
            double right = evalOperand(player, expression.substring(operatorIndex + operator.length()), vars);
            switch (operator) {
                case ">=": return left >= right;
                case "<=": return left <= right;
                case "==": return Double.compare(left, right) == 0;
                case "!=": return Double.compare(left, right) != 0;
                case ">": return left > right;
                default: return left < right;
            }
        }
        return evalOperand(player, expression, vars) > 0D;
    }

    private static double evalOperand(Player player, String operand, Map<String, Double> vars) {
        String trimmed = operand.trim();
        double sentinel = Double.NaN;
        double result = eval(player, "<c:" + trimmed + ">", vars, sentinel);
        if (Double.isNaN(result)) throw new IllegalArgumentException("invalid operand: " + trimmed);
        return result;
    }

    private static String unwrapCalculation(String formula) {
        String trimmed = formula.trim();
        return trimmed.startsWith("<c:") && trimmed.endsWith(">")
                ? trimmed.substring(3, trimmed.length() - 1).trim()
                : trimmed;
    }

    /** 查找变量标记之外的运算符，防止把 {@code <l:key>} 的结束符误判成大于号。 */
    private static int findOutsideToken(String expression, String operator) {
        boolean inToken = false;
        for (int index = 0; index <= expression.length() - operator.length(); index++) {
            char character = expression.charAt(index);
            if (!inToken && character == '<' && index + 2 < expression.length()
                    && expression.charAt(index + 2) == ':') {
                inToken = true;
                continue;
            }
            if (inToken) {
                if (character == '>') inToken = false;
                continue;
            }
            if (expression.startsWith(operator, index)) return index;
        }
        return -1;
    }
}
