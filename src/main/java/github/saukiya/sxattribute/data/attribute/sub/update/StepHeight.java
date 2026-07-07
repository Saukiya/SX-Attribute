package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 自动上阶 (无需跳跃可跨越的方块高度) - 原版 step_height, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class StepHeight extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "step_height"; }
    @Override protected String legacyName() { return "GENERIC_STEP_HEIGHT"; }
    @Override protected String discernName() { return "自动上阶"; }
    @Override protected double baseValue() { return 0.6; }
    @Override protected Mode mode() { return Mode.ADD; }
}
