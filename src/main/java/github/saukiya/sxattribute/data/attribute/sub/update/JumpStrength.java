package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 跳跃力 (跳跃初速度, 影响跳跃高度) - 原版 jump_strength, 1.20.5+ 通用化
 *
 * @author Ray_Hughes
 */
public class JumpStrength extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "jump_strength"; }
    @Override protected String legacyName() { return "GENERIC_JUMP_STRENGTH"; }
    @Override protected String discernName() { return "跳跃力"; }
    @Override protected double baseValue() { return 0.42; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
