package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 横扫比例 (剑横扫对副目标的伤害占比 0~1) - 原版 sweeping_damage_ratio, 1.21+
 *
 * @author Ray_Hughes
 */
public class Sweeping extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "sweeping_damage_ratio"; }
    @Override protected String legacyName() { return "PLAYER_SWEEPING_DAMAGE_RATIO"; }
    @Override protected String discernName() { return "横扫比例"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.RATIO; }
}
