package github.saukiya.sxattribute.api;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageTempData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DamageAPI {

    /**
     * 受攻击者伤害数据索引
     */
    private static final ConcurrentHashMap<UUID, List<DamageTempData>> victim = new ConcurrentHashMap<>();

    public static void addDamageData(UUID uuid, DamageTempData damageData) {
        victim.computeIfAbsent(uuid, k -> new ArrayList<>()).add(damageData);
        System.out.println("DamageAPI.addDamageData: " + uuid + " " + damageData);
    }

    public static SXAttributeData getDamageData(UUID caster, UUID target) {
        SXAttributeData attributeData = new SXAttributeData();
        if (!victim.containsKey(target)) {
            return null;
        }
        for (DamageTempData data : victim.get(target)) {
            if (data.getDamager().equals(caster)) {
                attributeData.add(data.getAttributes());
            }
        }
        System.out.println("DamageAPI.getDamageData: " + caster + " " + target + " " + attributeData);
        return attributeData;
    }

    public static void removeDamageData(UUID uuid) {
        victim.remove(uuid);
        System.out.println("DamageAPI.removeDamageData: " + uuid);
    }

    public static void removeByCaster(UUID caster) {
        for (List<DamageTempData> list : victim.values()) {
            list.removeIf(data -> data.getDamager().equals(caster));
        }
        System.out.println("DamageAPI.removeByCaster: " + caster);
    }
}
