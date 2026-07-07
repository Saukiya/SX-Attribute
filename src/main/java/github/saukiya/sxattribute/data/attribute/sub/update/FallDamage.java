package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 摔伤倍率 (摔落伤害倍数, 增幅 -100 即免摔伤) - 原版 fall_damage_multiplier, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class FallDamage extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "fall_damage_multiplier"; }
    @Override protected String legacyName() { return "GENERIC_FALL_DAMAGE_MULTIPLIER"; }
    @Override protected String discernName() { return "摔伤倍率"; }
    @Override protected double baseValue() { return 1.0; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
