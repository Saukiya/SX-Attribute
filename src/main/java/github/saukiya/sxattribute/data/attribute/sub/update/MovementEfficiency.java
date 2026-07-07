package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 移动效率 (减弱灵魂沙/黏液等减速 0~1) - 原版 movement_efficiency, 1.21+
 *
 * @author Ray_Hughes
 */
public class MovementEfficiency extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "movement_efficiency"; }
    @Override protected String legacyName() { return "GENERIC_MOVEMENT_EFFICIENCY"; }
    @Override protected String discernName() { return "移动效率"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.RATIO; }
}
