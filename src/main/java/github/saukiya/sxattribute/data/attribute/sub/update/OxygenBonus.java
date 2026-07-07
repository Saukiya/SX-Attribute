package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 氧气加成 (减缓水下缺氧) - 原版 oxygen_bonus, 1.21+
 *
 * @author Ray_Hughes
 */
public class OxygenBonus extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "oxygen_bonus"; }
    @Override protected String legacyName() { return "GENERIC_OXYGEN_BONUS"; }
    @Override protected String discernName() { return "氧气加成"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
