package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 空气阻力修正 (空气阻力倍率) - 原版 air_drag_modifier, 26.2+
 *
 * @author Ray_Hughes
 */
public class AirDragModifier extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "air_drag_modifier"; }
    @Override protected String legacyName() { return "AIR_DRAG_MODIFIER"; }
    @Override protected String discernName() { return "空气阻力修正"; }
    @Override protected double baseValue() { return 1.0; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
