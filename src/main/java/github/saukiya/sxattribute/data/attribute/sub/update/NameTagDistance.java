package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 名牌距离 (名牌可见距离) - 原版 name_tag_distance, 26.2+
 *
 * @author Ray_Hughes
 */
public class NameTagDistance extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "name_tag_distance"; }
    @Override protected String legacyName() { return "NAME_TAG_DISTANCE"; }
    @Override protected String discernName() { return "名牌距离"; }
    @Override protected double baseValue() { return 0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
