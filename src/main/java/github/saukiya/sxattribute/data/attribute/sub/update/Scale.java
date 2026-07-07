package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 体型增幅 (缩放模型与碰撞箱尺寸) - 原版 scale, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class Scale extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "scale"; }
    @Override protected String legacyName() { return "GENERIC_SCALE"; }
    @Override protected String discernName() { return "体型增幅"; }
    @Override protected double baseValue() { return 1.0; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
