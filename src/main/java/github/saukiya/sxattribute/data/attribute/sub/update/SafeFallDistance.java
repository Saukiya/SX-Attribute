package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 摔落保护 (不受摔伤的安全下落高度) - 原版 safe_fall_distance, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class SafeFallDistance extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "safe_fall_distance"; }
    @Override protected String legacyName() { return "GENERIC_SAFE_FALL_DISTANCE"; }
    @Override protected String discernName() { return "摔落保护"; }
    @Override protected double baseValue() { return 3.0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
