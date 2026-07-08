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
}
