package github.saukiya.sxattribute.feature.equipment.core;

import github.saukiya.sxattribute.event.SXLoadAttributeEvent;

/** 需要同时观察全部已装备物品的功能，例如套装和混搭奖励。 */
public interface AggregateEquipmentFeature {
    void contribute(SXLoadAttributeEvent event);
}
