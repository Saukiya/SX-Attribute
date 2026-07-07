package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 定位栏发送范围 (Locator Bar 广播范围) - 原版 waypoint_transmit_range, 1.21.6+
 *
 * @author Ray_Hughes
 */
public class WaypointTransmit extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "waypoint_transmit_range"; }
    @Override protected String legacyName() { return "WAYPOINT_TRANSMIT_RANGE"; }
    @Override protected String discernName() { return "定位栏发送范围"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
