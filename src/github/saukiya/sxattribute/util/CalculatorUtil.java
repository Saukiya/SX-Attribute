package github.saukiya.sxattribute.util;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 计算器工具类
 * 来源: CSDN
 * 方法来源于网站: https://blog.csdn.net/qq_37969433/article/details/81200872
 * 标题: （算法）java完成解析数学算式（计算器）三 —— 用栈解析
 *
 * @author Sirm23333
 */
public class CalculatorUtil {

    static Pattern p = Pattern.compile("(?<!\\d)-?\\d+(\\.\\d+)?|[+\\-*/%()]");// 这个正则为匹配表达式中的数字或运算符

    private static double doubleCal(double a1, double a2, char operator) throws Exception {
        switch (operator) {
            case '+':
                return a1 + a2;
            case '-':
                return a1 - a2;
            case '*':
                return a1 * a2;
            case '/':
                return a1 / a2;
            case '%':
                return a1 % a2;
            default:
                break;
        }
        throw new Exception("illegal operator!");
    }

    private static int getPriority(String s) throws Exception {
        if (s == null) {
            return 0;
        }
        switch (s) {
            case "(":
                return 1;
            case "+":
            case "-":
                return 2;
            case "*":
            case "%":
            case "/":
                return 3;
            default:
                break;
        }
        throw new Exception("illegal operator!");
    }

    public static Number getResult(String expr) throws Exception {
        boolean intTransform = false;
        if (expr.startsWith("int")) {
            intTransform = true;
            expr = expr.substring(3);
        }
        /*数字栈*/
        Stack<Double> number = new Stack<>();
        /*符号栈*/
        Stack<String> operator = new Stack<>();
        operator.push(null);// 在栈顶压人一个null，配合它的优先级，目的是减少下面程序的判断

        /* 将expr打散为运算数和运算符 */
        Matcher m = p.matcher(expr);
        while (m.find()) {
            String temp = m.group();
            if (temp.matches("[+\\-*/%()]")) {//遇到符号
                if (temp.equals("(")) {//遇到左括号，直接入符号栈
                    operator.push(temp);
                } else if (temp.equals(")")) {//遇到右括号，"符号栈弹栈取栈顶符号b，数字栈弹栈取栈顶数字a1，数字栈弹栈取栈顶数字a2，计算a2 b a1 ,将结果压入数字栈"，重复引号步骤至取栈顶为左括号，将左括号弹出
                    String b;
                    while (!(b = operator.pop()).equals("(")) {
                        double a1 = number.pop();
                        double a2 = number.pop();
                        number.push(doubleCal(a2, a1, b.charAt(0)));
                    }
                } else {//遇到运算符，满足该运算符的优先级大于栈顶元素的优先级压栈；否则计算后压栈
                    while (getPriority(temp) <= getPriority(operator.peek())) {
                        double a1 = number.pop();
                        double a2 = number.pop();
                        String b = operator.pop();
                        number.push(doubleCal(a2, a1, b.charAt(0)));
                    }
                    operator.push(temp);
                }
            } else {//遇到数字，直接压入数字栈
                number.push(Double.valueOf(temp));
            }
        }

        while (operator.peek() != null) {//遍历结束后，符号栈数字栈依次弹栈计算，并将结果压入数字栈
            double a1 = number.pop();
            double a2 = number.pop();
            String b = operator.pop();
            number.push(doubleCal(a2, a1, b.charAt(0)));
        }
        return intTransform ? Math.round(number.pop()) : number.pop();
    }
}
