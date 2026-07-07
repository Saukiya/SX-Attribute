package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 爆炸击退抗性 (抵抗爆炸击退 0~1) - 原版 explosion_knockback_resistance, 1.21+
 *
 * @author Ray_Hughes
 */
public class ExplosionKnockback extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "explosion_knockback_resistance"; }
    @Override protected String legacyName() { return "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE"; }
    @Override protected String discernName() { return "爆炸击退抗性"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.RATIO; }
}
