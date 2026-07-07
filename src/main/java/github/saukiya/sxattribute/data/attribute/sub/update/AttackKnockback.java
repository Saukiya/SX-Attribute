package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 攻击击退 (攻击附带的额外击退) - 原版 attack_knockback, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class AttackKnockback extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "attack_knockback"; }
    @Override protected String legacyName() { return "GENERIC_ATTACK_KNOCKBACK"; }
    @Override protected String discernName() { return "攻击击退"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
