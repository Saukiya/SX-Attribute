package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 挖掘效率 (使用正确工具时的挖掘加成) - 原版 mining_efficiency, 1.21+
 *
 * @author Ray_Hughes
 */
public class MiningEfficiency extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "mining_efficiency"; }
    @Override protected String legacyName() { return "PLAYER_MINING_EFFICIENCY"; }
    @Override protected String discernName() { return "挖掘效率"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
