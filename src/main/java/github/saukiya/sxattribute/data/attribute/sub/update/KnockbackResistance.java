package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 击退抗性 (抵抗击退 0~1, 1=完全免疫) - 原版 knockback_resistance
 *
 * @author Ray_Hughes
 */
public class KnockbackResistance extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "knockback_resistance"; }
    @Override protected String legacyName() { return "GENERIC_KNOCKBACK_RESISTANCE"; }
    @Override protected String discernName() { return "击退抗性"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.RATIO; }
}
