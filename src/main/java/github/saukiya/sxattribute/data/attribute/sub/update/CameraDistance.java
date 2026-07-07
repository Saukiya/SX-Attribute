package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 视距 (第三人称相机距离) - 原版 camera_distance, 1.21.6+
 *
 * @author Ray_Hughes
 */
public class CameraDistance extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "camera_distance"; }
    @Override protected String legacyName() { return "CAMERA_DISTANCE"; }
    @Override protected String discernName() { return "视距"; }
    @Override protected double baseValue() { return 4.0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
