package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 持久化属性源管理器 (跨重连/重启存活)
 * <p>
 * 高版本(1.14+): 用 MC 原生 {@link PersistentDataContainer} 存于玩家数据, 随存档自动持久化, 无需自管文件。
 * 低版本(&lt;1.14, 无 PDC): 回退到"每玩家一个独立 yml"({@code PersistentSource/<uuid>.yml})。
 * PDC 相关类被隔离在 {@link PdcStore} 内, 低版本不实例化即不加载, 保证跨版本安全。源名请勿包含 '.'。
 *
 * @author Ray_Hughes
 */
public class PersistentSourceManager {

    private final Store store;

    public PersistentSourceManager() {
        this.store = SXAttribute.isVersionAtLeast(14) ? new PdcStore() : new YmlStore();
    }

    /**
     * @return 玩家全部持久化源 (源名 -> 属性词条行)
     */
    public Map<String, List<String>> getSources(Player player) {
        return store.read(player);
    }

    /**
     * 新增/覆盖一个持久化源并写盘
     */
    public void add(Player player, String source, List<String> lore) {
        Map<String, List<String>> sources = store.read(player);
        sources.put(source, lore);
        store.write(player, sources);
    }

    /**
     * 删除一个持久化源
     *
     * @return 是否存在并删除
     */
    public boolean remove(Player player, String source) {
        Map<String, List<String>> sources = store.read(player);
        if (sources.remove(source) == null) {
            return false;
        }
        store.write(player, sources);
        return true;
    }

    /**
     * 上线时重新施加玩家全部持久化源(静默批量, 末尾统一刷新一次)
     */
    public void apply(Player player) {
        Map<String, List<String>> sources = store.read(player);
        if (sources.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : sources.entrySet()) {
            SXAttributeData data = SXAttribute.getAttributeManager().loadListData(entry.getValue());
            SXAttribute.getAttributeManager().addSource(player, entry.getKey(), data, false, false);
        }
        SXAttribute.getAttributeManager().attributeUpdateEvent(player);
    }

    /**
     * 存储策略: 源名 -> 属性词条行
     */
    private interface Store {
        Map<String, List<String>> read(Player player);

        void write(Player player, Map<String, List<String>> sources);
    }

    /**
     * MC 原生 PersistentDataContainer 存储 (1.14+, 随玩家存档自动持久化)。
     * 序列化为一段 YAML 字符串存入 PDC 的单个 STRING 键。
     */
    private static class PdcStore implements Store {

        private final NamespacedKey key = new NamespacedKey(SXAttribute.getInst(), "persistent_source");

        @Override
        public Map<String, List<String>> read(Player player) {
            Map<String, List<String>> sources = new LinkedHashMap<>();
            PersistentDataContainer container = player.getPersistentDataContainer();
            String raw = container.get(key, PersistentDataType.STRING);
            if (raw != null && !raw.isEmpty()) {
                YamlConfiguration yaml = new YamlConfiguration();
                try {
                    yaml.loadFromString(raw);
                    for (String source : yaml.getKeys(false)) {
                        sources.put(source, yaml.getStringList(source));
                    }
                } catch (InvalidConfigurationException ignored) {
                    // 数据损坏则视为空
                }
            }
            return sources;
        }

        @Override
        public void write(Player player, Map<String, List<String>> sources) {
            YamlConfiguration yaml = new YamlConfiguration();
            sources.forEach(yaml::set);
            player.getPersistentDataContainer().set(key, PersistentDataType.STRING, yaml.saveToString());
        }
    }

    /**
     * 低版本回退: 每玩家一个独立 yml。
     */
    private static class YmlStore implements Store {

        private final File dir = new File(SXAttribute.getInst().getDataFolder(), "PersistentSource");

        private File file(Player player) {
            return new File(dir, player.getUniqueId() + ".yml");
        }

        @Override
        public Map<String, List<String>> read(Player player) {
            Map<String, List<String>> sources = new LinkedHashMap<>();
            File file = file(player);
            if (file.exists()) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                for (String source : yaml.getKeys(false)) {
                    sources.put(source, yaml.getStringList(source));
                }
            }
            return sources;
        }

        @Override
        public void write(Player player, Map<String, List<String>> sources) {
            dir.mkdirs();
            YamlConfiguration yaml = new YamlConfiguration();
            sources.forEach(yaml::set);
            try {
                yaml.save(file(player));
            } catch (IOException e) {
                SXAttribute.getInst().getLogger().warning("Save PersistentSource yml failed: " + e.getMessage());
            }
        }
    }
}
