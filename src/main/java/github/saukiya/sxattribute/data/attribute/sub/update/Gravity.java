package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 重力 (下落加速度) - 原版 gravity, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class Gravity extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "gravity"; }
    @Override protected String legacyName() { return "GENERIC_GRAVITY"; }
    @Override protected String discernName() { return "重力"; }
    @Override protected double baseValue() { return 0.08; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
