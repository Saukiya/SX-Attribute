package github.saukiya.sxattribute.data.attribute.sub.update;

/**
 * 攻击距离 (玩家攻击/交互实体的距离) - 原版 entity_interaction_range, 1.20.5+
 *
 * @author Ray_Hughes
 */
public class AttackRange extends VanillaUpdateAttribute {
    @Override protected String registryKey() { return "entity_interaction_range"; }
    @Override protected String legacyName() { return "PLAYER_ENTITY_INTERACTION_RANGE"; }
    @Override protected String discernName() { return "攻击距离"; }
    @Override protected double baseValue() { return 3.0; }
    @Override protected Mode mode() { return Mode.ADD; }
}
