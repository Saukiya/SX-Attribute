package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 水中移动效率 (减弱水中移动惩罚 0~1) - 原版 water_movement_efficiency, 1.21+
 *
 * @author Ray_Hughes
 */
public class WaterMovement extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "water_movement_efficiency"; }
    @Override protected String legacyName() { return "GENERIC_WATER_MOVEMENT_EFFICIENCY"; }
    @Override protected String discernName() { return "水中移动效率"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.RATIO; }
}
