package github.saukiya.sxattribute.api;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageTempData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DamageAPI {

    /**
     * 受攻击者伤害数据索引
     */
    private static final ConcurrentHashMap<UUID, List<DamageTempData>> victimCache = new ConcurrentHashMap<>();

    public static void addDamageData(UUID uuid, DamageTempData damageData) {
        victimCache.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(damageData);
    }

    public static SXAttributeData getDamageData(UUID caster, UUID target) {
        List<DamageTempData> dataList = victimCache.get(target);
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        SXAttributeData attributeData = new SXAttributeData();
        dataList.stream()
                .filter(data -> data.getDamager().equals(caster))
                .forEach(data -> attributeData.add(data.getAttributes()));

        return attributeData;
    }

    public static void removeDamageData(UUID uuid) {
        victimCache.remove(uuid);
    }

    public static void removeByCaster(UUID caster) {
        victimCache.values().forEach(list -> list.removeIf(data -> data.getDamager().equals(caster)));
    }
}
