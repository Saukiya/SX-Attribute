package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 燃烧时长 (着火持续时间倍率, 增幅 -100 即免燃) - 原版 burning_time, 1.21+
 *
 * @author Ray_Hughes
 */
public class BurningTime extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "burning_time"; }
    @Override protected String legacyName() { return "GENERIC_BURNING_TIME"; }
    @Override protected String discernName() { return "燃烧时长"; }
    @Override protected double baseValue() { return 1.0; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
