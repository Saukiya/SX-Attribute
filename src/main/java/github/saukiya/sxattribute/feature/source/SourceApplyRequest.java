package github.saukiya.sxattribute.feature.source;

import github.saukiya.sxattribute.util.FormulaUtil;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 施加属性源的不可变请求。
 * <p>
 * API 与动作 DSL 共用该结构，确保叠层、持久化和生命周期语义只有一个入口。
 */
@Getter
public final class SourceApplyRequest {

    private final String source;
    private final List<String> attributes;
    private final long durationTicks;
    private final int maxStacks;
    private final StackMode stackMode;
    private final boolean persistent;
    private final List<String> tags;

    public SourceApplyRequest(String source, List<String> attributes, long durationTicks, int maxStacks,
                              StackMode stackMode, boolean persistent, List<String> tags) {
        this.source = source;
        this.attributes = Collections.unmodifiableList(new ArrayList<>(attributes));
        this.durationTicks = durationTicks;
        this.maxStacks = Math.max(1, maxStacks);
        this.stackMode = stackMode;
        this.persistent = persistent;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
    }

    @SuppressWarnings("unchecked")
    public static SourceApplyRequest fromAction(String definitionId, Map<String, Object> action,
                                                Map<String, Double> variables, Player player) {
        String source = String.valueOf(action.getOrDefault("Source", "attribute:" + definitionId));
        List<String> attributes = action.get("Attributes") instanceof List
                ? (List<String>) action.get("Attributes") : Collections.emptyList();
        List<String> tags = action.get("Tags") instanceof List ? (List<String>) action.get("Tags") : Collections.emptyList();
        long duration = (long) numeric(player, action.get("Duration"), variables, 0D);
        int maxStacks = (int) numeric(player, action.get("MaxStacks"), variables, 1D);
        StackMode mode;
        try {
            mode = StackMode.valueOf(String.valueOf(action.getOrDefault("StackMode", "REPLACE")).toUpperCase());
        } catch (IllegalArgumentException ignored) {
            mode = StackMode.REPLACE;
        }
        boolean persistent = Boolean.parseBoolean(String.valueOf(action.getOrDefault("Persistent", false)));
        return new SourceApplyRequest(source, attributes, duration, maxStacks, mode, persistent, tags);
    }

    private static double numeric(Player player, Object value, Map<String, Double> variables, double fallback) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        return value == null ? fallback : FormulaUtil.eval(player, String.valueOf(value), new LinkedHashMap<>(variables), fallback);
    }

    public enum StackMode {REPLACE, REFRESH, STACK, MAX, MIN, UNIQUE}
}
