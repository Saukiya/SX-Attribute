package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 飞行速度 (生物飞行速度) - 原版 flying_speed
 *
 * @author Ray_Hughes
 */
public class FlyingSpeed extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "flying_speed"; }
    @Override protected String legacyName() { return "GENERIC_FLYING_SPEED"; }
    @Override protected String discernName() { return "飞行速度"; }
    @Override protected double baseValue() { return 0.4; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
