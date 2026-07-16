package github.saukiya.sxattribute.feature.source;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQLite、MySQL 与 PostgreSQL 共用的 JDBC 来源仓库。
 * <p>
 * 更新采用“事务内读取版本 + 条件更新”，不依赖数据库私有 UPSERT 语法，三种后端保持
 * 完全相同的乐观锁行为。
 */
public final class SqlSourceRepository implements SourceRepository {

    private static final String TABLE = "sxa_attribute_sources";
    private final HikariDataSource dataSource;

    public SqlSourceRepository(ConfigurationSection storage, File dataFolder) {
        String type = storage.getString("Type", "SQLITE").toUpperCase();
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(Math.max(1, storage.getInt("PoolSize", 4)));
        config.setPoolName("SXAttribute-Source");
        if ("SQLITE".equals(type)) {
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + new File(dataFolder, storage.getString("SQLiteFile", "source-data.db")).getAbsolutePath());
            config.setMaximumPoolSize(1);
        } else if ("POSTGRESQL".equals(type)) {
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl("jdbc:postgresql://" + storage.getString("Host") + ":" + storage.getInt("Port", 5432)
                    + "/" + storage.getString("Database") + parameters(storage));
            config.setUsername(storage.getString("Username"));
            config.setPassword(storage.getString("Password"));
        } else {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + storage.getString("Host") + ":" + storage.getInt("Port", 3306)
                    + "/" + storage.getString("Database") + parameters(storage));
            config.setUsername(storage.getString("Username"));
            config.setPassword(storage.getString("Password"));
        }
        dataSource = new HikariDataSource(config);
        createSchema();
    }

    private String parameters(ConfigurationSection storage) {
        String value = storage.getString("Parameters", "");
        return value == null || value.isEmpty() ? "" : "?" + value;
    }

    private void createSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                + "player_uuid VARCHAR(36) NOT NULL, source_key VARCHAR(191) NOT NULL, payload TEXT NOT NULL, "
                + "stacks INTEGER NOT NULL, expires_at BIGINT NOT NULL, version BIGINT NOT NULL, "
                + "updated_by VARCHAR(64) NOT NULL, PRIMARY KEY (player_uuid, source_key))";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create source schema", exception);
        }
    }

    @Override
    public List<SourceRecord> load(UUID playerId) throws Exception {
        List<SourceRecord> records = new ArrayList<>();
        String sql = "SELECT source_key,payload,stacks,expires_at,version,updated_by FROM " + TABLE + " WHERE player_uuid=?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    YamlConfiguration payload = new YamlConfiguration();
                    payload.loadFromString(result.getString("payload"));
                    records.add(new SourceRecord(playerId, result.getString("source_key"), payload.getStringList("Attributes"),
                            result.getInt("stacks"), result.getLong("expires_at"), result.getLong("version"),
                            result.getString("updated_by"), payload.getStringList("Tags")));
                }
            }
        }
        return records;
    }

    @Override
    public boolean save(SourceRecord record, long expectedVersion) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            long current = currentVersion(connection, record.getPlayerId(), record.getSource());
            if (current != expectedVersion) {
                connection.rollback();
                return false;
            }
            String payload = payload(record);
            int changed;
            if (current < 0) {
                String insert = "INSERT INTO " + TABLE + " (player_uuid,source_key,payload,stacks,expires_at,version,updated_by) VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    bind(statement, record, payload, 1L);
                    changed = statement.executeUpdate();
                }
            } else {
                String update = "UPDATE " + TABLE + " SET payload=?,stacks=?,expires_at=?,version=?,updated_by=? "
                        + "WHERE player_uuid=? AND source_key=? AND version=?";
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setString(1, payload);
                    statement.setInt(2, record.getStacks());
                    statement.setLong(3, record.getExpiresAt());
                    statement.setLong(4, current + 1L);
                    statement.setString(5, record.getUpdatedBy());
                    statement.setString(6, record.getPlayerId().toString());
                    statement.setString(7, record.getSource());
                    statement.setLong(8, current);
                    changed = statement.executeUpdate();
                }
            }
            if (changed != 1) {
                connection.rollback();
                return false;
            }
            connection.commit();
            return true;
        }
    }

    @Override
    public boolean delete(UUID playerId, String source, long expectedVersion) throws Exception {
        String sql = "DELETE FROM " + TABLE + " WHERE player_uuid=? AND source_key=? AND version=?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, source);
            statement.setLong(3, expectedVersion);
            return statement.executeUpdate() == 1;
        }
    }

    private long currentVersion(Connection connection, UUID playerId, String source) throws Exception {
        String sql = "SELECT version FROM " + TABLE + " WHERE player_uuid=? AND source_key=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, source);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : -1L;
            }
        }
    }

    private String payload(SourceRecord record) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("Attributes", record.getAttributes());
        yaml.set("Tags", record.getTags());
        return yaml.saveToString();
    }

    private void bind(PreparedStatement statement, SourceRecord record, String payload, long version) throws Exception {
        statement.setString(1, record.getPlayerId().toString());
        statement.setString(2, record.getSource());
        statement.setString(3, payload);
        statement.setInt(4, record.getStacks());
        statement.setLong(5, record.getExpiresAt());
        statement.setLong(6, version);
        statement.setString(7, record.getUpdatedBy());
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
