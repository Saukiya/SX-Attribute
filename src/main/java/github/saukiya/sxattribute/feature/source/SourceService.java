package github.saukiya.sxattribute.feature.source;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeSource;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.event.SXSourceWriteEvent;
import github.saukiya.sxattribute.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * 命名属性源生命周期与持久化服务。
 * <p>
 * 非持久化来源只存在内存；持久化来源先取得 Redis 锁并提交 SQL，成功后才改变本服实体。
 * 多服模式 Redis 不可用时严格拒绝写入，防止两个服务器产生不可合并的来源版本。
 */
public class SourceService implements Listener {

    private final Map<UUID, Map<String, ManagedSource>> managed = new ConcurrentHashMap<>();
    /** 到期任务只保存实体引用并通过实体调度器操作，不在全局线程直接查找或修改实体。 */
    private final Map<UUID, LivingEntity> trackedEntities = new ConcurrentHashMap<>();
    private YamlConfiguration config;
    private SourceRepository repository = new MemorySourceRepository();
    private RedisCoordinator redis;
    private boolean enabled;
    private boolean networkMode;
    private String storageType;
    private String serverId;
    private Object expiryTask;

    public SourceService() {
        Bukkit.getPluginManager().registerEvents(this, SXAttribute.getInst());
        reload();
    }

    public final void reload() {
        closeBackends();
        File file = new File(SXAttribute.getInst().getDataFolder(), "Feature" + File.separator + "Source" + File.separator + "Config.yml");
        if (!file.exists()) SXAttribute.getInst().saveResource("Feature/Source/Config.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
        enabled = config.getBoolean("Enable", true);
        if (!enabled) clearManagedEffects();
        networkMode = config.getBoolean("NetworkMode", false);
        serverId = config.getString("ServerId", "server-1");
        ConfigurationSection storage = config.getConfigurationSection("Storage");
        storageType = storage == null ? "MEMORY" : storage.getString("Type", "MEMORY").toUpperCase();
        if (enabled && !"MEMORY".equals(storageType)) {
            if (networkMode && "SQLITE".equals(storageType)) {
                SXAttribute.getInst().getLogger().severe("SQLite is single-server only; persistent writes will be rejected in NetworkMode.");
            } else {
                repository = new SqlSourceRepository(storage, new File(SXAttribute.getInst().getDataFolder(), "Feature/Source"));
            }
        }
        ConfigurationSection redisSection = config.getConfigurationSection("Redis");
        if (enabled && redisSection != null && redisSection.getBoolean("Enable", false)) {
            redis = new RedisCoordinator(redisSection);
            redis.subscribe(this::onInvalidation);
        }
        long period = Math.max(1L, config.getLong("CheckPeriodTicks", 10L));
        expiryTask = FoliaScheduler.runTimer(SXAttribute.getInst(), this::expire, period, period);
    }

    /** 文本来源同时保留持久化表示，所有入口共用叠层与写入事件。 */
    public SourceWriteResult apply(LivingEntity entity, SourceApplyRequest request) {
        return apply(entity, request, null);
    }

    /**
     * 施加运行时属性快照，用于召唤物继承全部旧数组/脚本字段；快照没有可重载的 Lore 表示，
     * 因此只允许非持久化 REPLACE，不得覆盖已有持久化来源或伪装成可持久化的文本来源。
     */
    public SourceWriteResult applySnapshot(LivingEntity entity, SourceApplyRequest request, SXAttributeData data) {
        if (data == null || request.isPersistent() || request.getStackMode() != SourceApplyRequest.StackMode.REPLACE) {
            return SourceWriteResult.FAILED;
        }
        return apply(entity, request, copySnapshot(data));
    }

    private SourceWriteResult apply(LivingEntity entity, SourceApplyRequest request, SXAttributeData snapshot) {
        if (!enabled || entity == null || request.getSource() == null || request.getSource().isEmpty()) return SourceWriteResult.FAILED;
        Map<String, ManagedSource> entitySources = managed.computeIfAbsent(entity.getUniqueId(), ignored -> new ConcurrentHashMap<>());
        trackedEntities.put(entity.getUniqueId(), entity);
        ManagedSource previous = entitySources.get(request.getSource());
        if (snapshot != null && previous != null && previous.persistent) return SourceWriteResult.FAILED;
        if (previous != null && request.getStackMode() == SourceApplyRequest.StackMode.UNIQUE) {
            return notify(entity, request.getSource(), SourceWriteResult.UNIQUE_EXISTS);
        }
        ManagedSource next = snapshot == null ? combine(entity, request, previous)
                : new ManagedSource(entity.getUniqueId(), request.getSource(), Collections.emptyList(), snapshot,
                1, expiry(request), -1L, false, new HashSet<>(request.getTags()));
        if (request.isPersistent()) {
            SourceWriteResult stored = persist(next, previous == null ? -1L : previous.version);
            if (stored != SourceWriteResult.APPLIED) return notify(entity, request.getSource(), stored);
        }
        entitySources.put(request.getSource(), next);
        SXAttribute.getAttributeManager().putSource(entity.getUniqueId(), new AttributeSource(request.getSource(), next.data, true));
        SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
        return notify(entity, request.getSource(), SourceWriteResult.APPLIED);
    }

    /** 修改已有来源的剩余时间；不重建数值或叠层，持久化来源仍走版本锁与存储失败反馈。 */
    public SourceWriteResult changeDuration(LivingEntity entity, String source, long durationTicks) {
        long now = System.currentTimeMillis();
        if (!enabled || entity == null || durationTicks < 0L || durationTicks > (Long.MAX_VALUE - now) / 50L) {
            return SourceWriteResult.FAILED;
        }
        Map<String, ManagedSource> sources = managed.get(entity.getUniqueId());
        ManagedSource previous = sources == null ? null : sources.get(source);
        if (previous == null) return SourceWriteResult.FAILED;
        ManagedSource next = previous.refresh(durationTicks == 0L ? 0L : now + durationTicks * 50L);
        if (next.persistent) {
            SourceWriteResult stored = persist(next, previous.version);
            if (stored != SourceWriteResult.APPLIED) return notify(entity, source, stored);
        }
        sources.put(source, next);
        return notify(entity, source, SourceWriteResult.APPLIED);
    }

    /**
     * 只计算已有运行时来源的数值，保留到期时间、层数与标签。持久化来源无法从任意数组逆推 Lore，
     * 因此明确拒绝；调用者应使用文本 REPLACE 重新定义可持久化属性。
     */
    public SourceWriteResult transformSnapshot(LivingEntity entity, String source, UnaryOperator<SXAttributeData> transform) {
        if (!enabled || entity == null) return SourceWriteResult.FAILED;
        Map<String, ManagedSource> sources = managed.get(entity.getUniqueId());
        ManagedSource previous = sources == null ? null : sources.get(source);
        if (previous == null || previous.persistent) return SourceWriteResult.FAILED;
        SXAttributeData transformed = transform.apply(copySnapshot(previous.data));
        if (transformed == null) return SourceWriteResult.FAILED;
        ManagedSource next = new ManagedSource(previous.playerId, source, Collections.emptyList(), copySnapshot(transformed),
                previous.stacks, previous.expiresAt, previous.version, false, new HashSet<>(previous.tags));
        sources.put(source, next);
        SXAttribute.getAttributeManager().putSource(entity.getUniqueId(), new AttributeSource(source, next.data, true));
        SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
        return notify(entity, source, SourceWriteResult.APPLIED);
    }

    /** 保留负数、MAX/LAST 原值，并在写源前拒绝倍率或加法溢出的非有限数值。 */
    private SXAttributeData copySnapshot(SXAttributeData original) {
        SXAttributeData copy = new SXAttributeData();
        for (int i = 0; i < original.getValues().length; i++) {
            for (double value : original.getValues()[i]) {
                if (!Double.isFinite(value)) throw new IllegalArgumentException("Source snapshot value must be finite");
            }
            System.arraycopy(original.getValues()[i], 0, copy.getValues()[i], 0, original.getValues()[i].length);
        }
        original.getDynamicValues().forEach((id, fields) -> {
            for (double value : fields.values()) {
                if (!Double.isFinite(value)) throw new IllegalArgumentException("Source snapshot value must be finite");
            }
            copy.getDynamicValues().put(id, new LinkedHashMap<>(fields));
        });
        return copy;
    }

    /** 按 Feature/Source/Config.yml 的 Rules 节点施加来源。 */
    public SourceWriteResult applyRule(LivingEntity entity, String ruleId) {
        ConfigurationSection rule = config.getConfigurationSection("Rules." + ruleId);
        if (rule == null) return SourceWriteResult.FAILED;
        Map<String, Double> variables = new LinkedHashMap<>();
        variables.put("health", entity.getHealth());
        variables.put("max_health", entity.getMaxHealth());
        Map<String, Object> action = new LinkedHashMap<>(rule.getValues(false));
        action.putIfAbsent("Source", "rule:" + ruleId);
        SourceApplyRequest request = SourceApplyRequest.fromAction(ruleId, action, variables,
                entity instanceof Player ? (Player) entity : null);
        return apply(entity, request);
    }

    public SourceWriteResult remove(LivingEntity entity, String source) {
        Map<String, ManagedSource> sources = managed.get(entity.getUniqueId());
        ManagedSource current = sources == null ? null : sources.get(source);
        if (current != null && current.persistent) {
            SourceWriteResult result = deletePersistent(current);
            if (result != SourceWriteResult.REMOVED) return notify(entity, source, result);
        }
        if (sources != null) sources.remove(source);
        SXAttribute.getAttributeManager().removeSource(entity.getUniqueId(), source);
        SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
        return notify(entity, source, SourceWriteResult.REMOVED);
    }

    private SourceWriteResult notify(LivingEntity entity, String source, SourceWriteResult result) {
        Bukkit.getPluginManager().callEvent(new SXSourceWriteEvent(entity, source, result));
        return result;
    }

    public boolean hasTag(UUID entityId, String tag) {
        Map<String, ManagedSource> sources = managed.get(entityId);
        return sources != null && sources.values().stream().anyMatch(source -> source.tags.contains(tag));
    }

    public Set<String> tags(UUID entityId) {
        Map<String, ManagedSource> sources = managed.get(entityId);
        if (sources == null) return Collections.emptySet();
        Set<String> tags = new HashSet<>();
        sources.values().forEach(source -> tags.addAll(source.tags));
        return tags;
    }

    /**
     * 导出来源层数与剩余 tick，供属性条件公式引用。
     */
    public Map<String, Double> formulaVariables(UUID entityId, String prefix) {
        Map<String, ManagedSource> sources = managed.get(entityId);
        if (sources == null) return Collections.emptyMap();
        Map<String, Double> variables = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        for (ManagedSource source : sources.values()) {
            String safe = source.source.replaceAll("[^A-Za-z0-9_]", "_");
            variables.put(prefix + "_source_" + safe + "_stacks", (double) source.stacks);
            double remaining = source.expiresAt <= 0L ? -1D : Math.max(0D, (source.expiresAt - now) / 50D);
            variables.put(prefix + "_source_" + safe + "_remaining", remaining);
        }
        return variables;
    }

    private ManagedSource combine(LivingEntity entity, SourceApplyRequest request, ManagedSource previous) {
        SXAttributeData oneLayer = SXAttribute.getAttributeManager().loadListData(request.getAttributes());
        int stacks = 1;
        SXAttributeData data = oneLayer;
        List<String> attributes = new ArrayList<>(request.getAttributes());
        if (previous != null) {
            switch (request.getStackMode()) {
                case REFRESH:
                    stacks = previous.stacks;
                    data = previous.data;
                    attributes = previous.attributes;
                    break;
                case STACK:
                    stacks = Math.min(request.getMaxStacks(), previous.stacks + 1);
                    data = new SXAttributeData();
                    for (int index = 0; index < stacks; index++) data.add(oneLayer);
                    break;
                case MAX:
                    if (score(previous.data) >= score(oneLayer)) return previous.refresh(expiry(request));
                    break;
                case MIN:
                    if (score(previous.data) <= score(oneLayer)) return previous.refresh(expiry(request));
                    break;
                case REPLACE:
                default:
                    break;
            }
        }
        long version = previous == null ? -1L : previous.version;
        return new ManagedSource(entity.getUniqueId(), request.getSource(), attributes, data, stacks, expiry(request),
                version, request.isPersistent(), new HashSet<>(request.getTags()));
    }

    private double score(SXAttributeData data) {
        return data.calculationCombatPower();
    }

    private long expiry(SourceApplyRequest request) {
        return request.getDurationTicks() <= 0L ? 0L : System.currentTimeMillis() + request.getDurationTicks() * 50L;
    }

    private SourceWriteResult persist(ManagedSource source, long expectedVersion) {
        // 继承/计算快照没有可恢复的 Lore；后续 REFRESH/MAX/MIN 也不能把这些数值伪装成持久化空源。
        if (source.attributes.isEmpty() && source.data.isValid()) return SourceWriteResult.FAILED;
        if ("MEMORY".equals(storageType) || networkMode && "SQLITE".equals(storageType)) return SourceWriteResult.STORAGE_DISABLED;
        RedisCoordinator.Lock lock = null;
        if (networkMode) {
            if (redis == null || !redis.available()) return SourceWriteResult.REDIS_UNAVAILABLE;
            lock = redis.acquire(source.playerId, source.source);
            if (lock == null) return SourceWriteResult.LOCK_CONFLICT;
        }
        try {
            SourceRecord record = source.record(serverId);
            if (!repository.save(record, expectedVersion)) return SourceWriteResult.VERSION_CONFLICT;
            source.version = expectedVersion < 0L ? 1L : expectedVersion + 1L;
            if (redis != null) redis.publish(source.playerId, source.source);
            return SourceWriteResult.APPLIED;
        } catch (Exception exception) {
            SXAttribute.getInst().getLogger().warning("Persistent source write failed: " + exception.getMessage());
            return SourceWriteResult.FAILED;
        } finally {
            if (redis != null) redis.release(lock);
        }
    }

    private SourceWriteResult deletePersistent(ManagedSource source) {
        if (networkMode && (redis == null || !redis.available())) return SourceWriteResult.REDIS_UNAVAILABLE;
        RedisCoordinator.Lock lock = networkMode ? redis.acquire(source.playerId, source.source) : null;
        if (networkMode && lock == null) return SourceWriteResult.LOCK_CONFLICT;
        try {
            if (!repository.delete(source.playerId, source.source, source.version)) return SourceWriteResult.VERSION_CONFLICT;
            if (redis != null) redis.publish(source.playerId, source.source);
            return SourceWriteResult.REMOVED;
        } catch (Exception exception) {
            return SourceWriteResult.FAILED;
        } finally {
            if (redis != null) redis.release(lock);
        }
    }

    private void expire() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Map<String, ManagedSource>> entityEntry : managed.entrySet()) {
            LivingEntity entity = trackedEntities.get(entityEntry.getKey());
            if (entity == null || !entity.isValid()) continue;
            FoliaScheduler.runEntity(entity, SXAttribute.getInst(), () -> expireEntity(entity, entityEntry.getValue(), now), 1L);
        }
    }

    private void expireEntity(LivingEntity entity, Map<String, ManagedSource> sources, long now) {
        boolean changed = false;
        for (ManagedSource source : new ArrayList<>(sources.values())) {
            if (source.expiresAt > 0L && source.expiresAt <= now) {
                sources.remove(source.source);
                SXAttribute.getAttributeManager().removeSource(entity.getUniqueId(), source.source);
                changed = true;
            }
        }
        if (changed) SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
    }

    private void loadPersistent(Player player) {
        if ("MEMORY".equals(storageType)) return;
        Map<String, List<String>> legacy = SXAttribute.getPersistentSourceManager() == null
                ? Collections.emptyMap() : SXAttribute.getPersistentSourceManager().getSources(player);
        CompletableFuture.runAsync(() -> {
            try {
                List<SourceRecord> records = repository.load(player.getUniqueId());
                FoliaScheduler.runEntity(player, SXAttribute.getInst(), () -> {
                    if (records.isEmpty() && !legacy.isEmpty()) importLegacy(player, legacy);
                    else applyLoaded(player, records);
                }, 1L);
            } catch (Exception exception) {
                SXAttribute.getInst().getLogger().warning("Persistent source load failed: " + exception.getMessage());
            }
        });
    }

    /**
     * 一次性导入旧 PDC/YAML 来源；旧数据不删除，天然作为迁移备份。
     */
    private void importLegacy(Player player, Map<String, List<String>> legacy) {
        for (Map.Entry<String, List<String>> entry : legacy.entrySet()) {
            apply(player, new SourceApplyRequest(entry.getKey(), entry.getValue(), 0L, 1,
                    SourceApplyRequest.StackMode.REPLACE, true, Collections.singletonList("legacy-import")));
        }
    }

    private void applyLoaded(Player player, List<SourceRecord> records) {
        long now = System.currentTimeMillis();
        Map<String, ManagedSource> playerSources = managed.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>());
        trackedEntities.put(player.getUniqueId(), player);
        // SQL 是事实源；先移除本服旧持久化快照，才能正确同步其它服务器上的删除操作。
        for (ManagedSource existing : new ArrayList<>(playerSources.values())) {
            if (existing.persistent) {
                playerSources.remove(existing.source);
                SXAttribute.getAttributeManager().removeSource(player.getUniqueId(), existing.source);
            }
        }
        for (SourceRecord record : records) {
            if (record.getExpiresAt() > 0L && record.getExpiresAt() <= now) continue;
            SXAttributeData layer = SXAttribute.getAttributeManager().loadListData(record.getAttributes());
            SXAttributeData data = new SXAttributeData();
            for (int index = 0; index < Math.max(1, record.getStacks()); index++) data.add(layer);
            ManagedSource source = new ManagedSource(player.getUniqueId(), record.getSource(), record.getAttributes(), data,
                    record.getStacks(), record.getExpiresAt(), record.getVersion(), true, new HashSet<>(record.getTags()));
            playerSources.put(source.source, source);
            SXAttribute.getAttributeManager().putSource(player.getUniqueId(), new AttributeSource(source.source, source.data, true));
        }
        SXAttribute.getAttributeManager().attributeUpdateEvent(player);
    }

    private void onInvalidation(String message) {
        String[] parts = message.split("[|]", 2);
        if (parts.length != 2) return;
        try {
            UUID playerId = UUID.fromString(parts[0]);
            // Redis 订阅线程不得读取 Bukkit 实体；先切到全局调度器，再切到玩家所属区域线程。
            FoliaScheduler.runSync(SXAttribute.getInst(), () -> {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    FoliaScheduler.runEntity(player, SXAttribute.getInst(), () -> loadPersistent(player), 1L);
                }
            }, 1L);
        } catch (IllegalArgumentException ignored) {
            SXAttribute.getInst().getLogger().warning("Invalid Redis source message: " + message);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadPersistent(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        managed.remove(event.getPlayer().getUniqueId());
        trackedEntities.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            managed.remove(event.getEntity().getUniqueId());
            trackedEntities.remove(event.getEntity().getUniqueId());
        }
    }

    public void disable() {
        FoliaScheduler.cancel(expiryTask);
        closeBackends();
        managed.clear();
        trackedEntities.clear();
    }

    private void closeBackends() {
        FoliaScheduler.cancel(expiryTask);
        expiryTask = null;
        if (redis != null) redis.close();
        redis = null;
        repository.close();
        repository = new MemorySourceRepository();
    }

    /** 关闭模块只移除运行时效果，SQL/PDC 中的持久化事实仍保留以便重新启用。 */
    private void clearManagedEffects() {
        for (Map.Entry<UUID, LivingEntity> entry : trackedEntities.entrySet()) {
            LivingEntity entity = entry.getValue();
            Map<String, ManagedSource> sources = managed.get(entry.getKey());
            if (entity == null || sources == null) continue;
            FoliaScheduler.runEntity(entity, SXAttribute.getInst(), () -> {
                for (String source : sources.keySet()) {
                    SXAttribute.getAttributeManager().removeSource(entity.getUniqueId(), source);
                }
                SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
            }, 1L);
        }
        managed.clear();
        trackedEntities.clear();
    }

    private static final class ManagedSource {
        private final UUID playerId;
        private final String source;
        private final List<String> attributes;
        private final SXAttributeData data;
        private final int stacks;
        private final long expiresAt;
        private long version;
        private final boolean persistent;
        private final Set<String> tags;

        private ManagedSource(UUID playerId, String source, List<String> attributes, SXAttributeData data, int stacks,
                              long expiresAt, long version, boolean persistent, Set<String> tags) {
            this.playerId = playerId;
            this.source = source;
            this.attributes = new ArrayList<>(attributes);
            this.data = data;
            this.stacks = stacks;
            this.expiresAt = expiresAt;
            this.version = version;
            this.persistent = persistent;
            this.tags = tags;
        }

        private ManagedSource refresh(long expiresAt) {
            return new ManagedSource(playerId, source, attributes, data, stacks, expiresAt, version, persistent, tags);
        }

        private SourceRecord record(String serverId) {
            return new SourceRecord(playerId, source, attributes, stacks, expiresAt, version, serverId, new ArrayList<>(tags));
        }
    }
}
