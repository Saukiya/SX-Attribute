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

    private static void update() {
        long currentTime = System.currentTimeMillis();
        cache.forEach((uuid, attributes) -> attributes.entrySet().removeIf(entry -> entry.getValue().getLast() < currentTime));
    }

    public static void setCache(UUID uuid, String name, SXAttributeData attributeData, long durationMillis) {
        long expiryTime = System.currentTimeMillis() + durationMillis;
        cache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(name, new Pair<>(attributeData, expiryTime));
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
        ConcurrentHashMap<String, Pair<SXAttributeData, Long>> attrs = cache.getOrDefault(uuid, new ConcurrentHashMap<>());
        attrs.forEach((name, pair) -> {
            if (pair.getLast() > System.currentTimeMillis()) {
                attributeData.add(pair.getFirst());
            } else {
                attrs.remove(name);
            }
        });
        return attributeData;
    }

    public static void removeCache(UUID uuid, String name) {
        ConcurrentHashMap<String, Pair<SXAttributeData, Long>> userCache = cache.get(uuid);
        if (userCache != null) {
            userCache.remove(name);
        }
    }

    public static void removeCache(UUID uuid) {
        cache.remove(uuid);
    }

}
