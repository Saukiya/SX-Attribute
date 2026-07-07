package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 交互距离 (玩家挖掘/放置方块的距离) - 原版 block_interaction_range, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class BlockRange extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "block_interaction_range"; }
    @Override protected String legacyName() { return "PLAYER_BLOCK_INTERACTION_RANGE"; }
    @Override protected String discernName() { return "交互距离"; }
    @Override protected double baseValue() { return 4.5; }
    @Override protected Mode mode() { return Mode.ADD; }
}
