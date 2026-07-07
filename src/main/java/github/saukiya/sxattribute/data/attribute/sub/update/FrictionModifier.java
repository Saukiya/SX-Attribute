package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 摩擦修正 (地面摩擦倍率) - 原版 friction_modifier, 26.2+
 *
 * @author Ray_Hughes
 */
public class FrictionModifier extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "friction_modifier"; }
    @Override protected String legacyName() { return "FRICTION_MODIFIER"; }
    @Override protected String discernName() { return "摩擦修正"; }
    @Override protected double baseValue() { return 1.0; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
