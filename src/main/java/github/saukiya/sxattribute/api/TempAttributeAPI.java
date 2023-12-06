package github.saukiya.sxattribute.api;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.util.Pair;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TempAttributeAPI {

    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Pair<SXAttributeData, Long>>> cache = new ConcurrentHashMap<>();

    public static void startUpdate() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SXAttribute.getInst(), TempAttributeAPI::update, 0L, 20L);
    }

    public static void update() {
        HashMap<UUID, ConcurrentHashMap<String, Pair<SXAttributeData, Long>>> cloneMap = new HashMap<>(cache);
        cloneMap.forEach((uuid, map) -> map.forEach((name, pair) -> {
            if (pair.getLast() < System.currentTimeMillis()) {
                cache.getOrDefault(uuid, new ConcurrentHashMap<>()).remove(name);
            }
        }));
    }

    public static void setCache(UUID uuid, String name, SXAttributeData attributeData, long time) {
        cache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(name, new Pair<>(attributeData, System.currentTimeMillis() + time));
    }

    public static SXAttributeData getCache(UUID uuid, String name) {
        Pair<SXAttributeData, Long> pair = cache.getOrDefault(uuid, new ConcurrentHashMap<>()).get(name);
        if (pair != null && pair.getLast() > System.currentTimeMillis()) {
            return pair.getFirst();
        }
        return null;
    }

    public static SXAttributeData getCache(UUID uuid) {
        SXAttributeData attributeData = new SXAttributeData();
        HashMap<UUID, ConcurrentHashMap<String, Pair<SXAttributeData, Long>>> cloneMap = new HashMap<>(cache);
        cloneMap.getOrDefault(uuid, new ConcurrentHashMap<>()).forEach((name, pair) -> {
            if (pair.getLast() > System.currentTimeMillis()) {
                attributeData.add(pair.getFirst());
            } else {
                cache.getOrDefault(uuid, new ConcurrentHashMap<>()).remove(name);
            }
        });
        return attributeData;
    }

    public static void removeCache(UUID uuid, String name) {
        cache.getOrDefault(uuid, new ConcurrentHashMap<>()).remove(name);
    }

    public static void removeCache(UUID uuid) {
        cache.remove(uuid);
    }

}
