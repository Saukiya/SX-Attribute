package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 水下挖掘速度 (水下挖掘速度系数) - 原版 submerged_mining_speed, 1.21+
 *
 * @author Ray_Hughes
 */
public class SubmergedMining extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "submerged_mining_speed"; }
    @Override protected String legacyName() { return "PLAYER_SUBMERGED_MINING_SPEED"; }
    @Override protected String discernName() { return "水下挖掘速度"; }
    @Override protected double baseValue() { return 0.2; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
