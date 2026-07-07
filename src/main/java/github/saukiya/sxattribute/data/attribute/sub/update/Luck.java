package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 幸运 (影响战利品表抽取, 如钓鱼) - 原版 luck
 *
 * @author Ray_Hughes
 */
public class Luck extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "luck"; }
    @Override protected String legacyName() { return "GENERIC_LUCK"; }
    @Override protected String discernName() { return "幸运"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
