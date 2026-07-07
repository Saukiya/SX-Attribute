package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 挖掘速度 (整体挖掘速度倍率) - 原版 block_break_speed, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class BlockBreakSpeed extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "block_break_speed"; }
    @Override protected String legacyName() { return "PLAYER_BLOCK_BREAK_SPEED"; }
    @Override protected String discernName() { return "挖掘速度"; }
    @Override protected double baseValue() { return 1.0; }
    @Override protected Mode mode() { return Mode.PERCENT; }
}
