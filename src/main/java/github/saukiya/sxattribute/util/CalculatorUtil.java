package github.saukiya.sxattribute.util;

public class CalculatorUtil {

    public static Number getResult(String expr) {
        boolean intTransform = false;
        if (expr.startsWith("int")) {
            intTransform = true;
            expr = expr.substring(3);
        }
        double result = github.saukiya.tools.util.CalculatorUtil.calculator(expr);
        return intTransform ? Math.round(result) : result;
    }
}
