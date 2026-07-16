package github.saukiya.sxattribute.feature.equipment.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 装备功能操作结果，用于 GUI 决定提交、回滚或销毁。 */
@AllArgsConstructor
@Getter
public final class FeatureResult {
    private final boolean success;
    /** 是否真正执行了玩法判定；前置条件不满足时为 false，成本必须退回。 */
    private final boolean accepted;
    private final boolean changed;
    private final boolean destroy;
    private final String message;

    public static FeatureResult success(String message) {
        return new FeatureResult(true, true, true, false, message);
    }

    public static FeatureResult failed(boolean changed, boolean destroy, String message) {
        return new FeatureResult(false, true, changed, destroy, message);
    }

    public static FeatureResult unsupported(String message) {
        return new FeatureResult(false, false, false, false, message);
    }
}
