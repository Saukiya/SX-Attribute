package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 定位栏接收范围 (Locator Bar 接收范围) - 原版 waypoint_receive_range, 1.21.6+
 *
 * @author Ray_Hughes
 */
public class WaypointReceive extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "waypoint_receive_range"; }
    @Override protected String legacyName() { return "WAYPOINT_RECEIVE_RANGE"; }
    @Override protected String discernName() { return "定位栏接收范围"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
