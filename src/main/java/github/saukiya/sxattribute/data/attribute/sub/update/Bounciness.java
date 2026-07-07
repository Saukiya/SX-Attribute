package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 弹性 (落地反弹系数 0~1) - 原版 bounciness, 26.2+
 *
 * @author Ray_Hughes
 */
public class Bounciness extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "bounciness"; }
    @Override protected String legacyName() { return "BOUNCINESS"; }
    @Override protected String discernName() { return "弹性"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.RATIO; }
}
