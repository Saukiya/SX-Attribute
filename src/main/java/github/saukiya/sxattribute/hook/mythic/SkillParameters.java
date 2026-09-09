package github.saukiya.sxattribute.hook.mythic;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 保留旧 MM 4 参数中的第二个等号。MM 4.1 对每项直接 split("=")，会截断 al=攻击力=100；
 * 因此从其仍完整保留的 getLine() 读取 SX 自有参数，不改变 MM 选择器、触发器与其它机制。
 */
public final class SkillParameters {
    private SkillParameters() { }

    /** 仅解析第一个机制参数块；SX/MM 标记、嵌套块和配对引号内不切分，忽略后面的选择器。 */
    public static Map<String, String> parse(String line) {
        Map<String, String> parameters = new LinkedHashMap<>();
        int open = line.indexOf('{');
        if (open < 0 || line.substring(0, open).contains("@")) return parameters;
        int depth = 1;
        int start = open + 1;
        char quote = 0;
        for (int index = start; index < line.length(); index++) {
            char character = line.charAt(index);
            if (quote != 0) {
                if (character == quote && line.charAt(index - 1) != '\\') quote = 0;
                continue;
            }
            if (character == '\'' || character == '"') {
                quote = character;
                continue;
            }
            int tokenEnd = SkillExpressions.tokenEnd(line, index);
            if (tokenEnd >= 0) {
                index = tokenEnd;
                continue;
            }
            if (character == '{') depth++;
            if (character == '}') depth--;
            if (depth == 0 || depth == 1 && character == ';') {
                String pair = line.substring(start, index);
                int equals = pair.indexOf('=');
                if (equals > 0) parameters.put(pair.substring(0, equals).trim().toLowerCase(Locale.ROOT), pair.substring(equals + 1).trim());
                start = index + 1;
            }
            if (depth == 0) return parameters;
        }
        throw new IllegalArgumentException("Unbalanced SX skill parameters: " + line);
    }
}
