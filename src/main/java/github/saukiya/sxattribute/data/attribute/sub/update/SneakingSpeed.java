package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 潜行速度 (潜行移动速度系数) - 原版 sneaking_speed, 1.21+
 *
 * @author Ray_Hughes
 */
public class SneakingSpeed extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "sneaking_speed"; }
    @Override protected String legacyName() { return "PLAYER_SNEAKING_SPEED"; }
    @Override protected String discernName() { return "潜行速度"; }
    @Override protected double baseValue() { return 0.3; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
