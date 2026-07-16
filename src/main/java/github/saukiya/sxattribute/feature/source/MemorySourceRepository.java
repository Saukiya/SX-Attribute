package github.saukiya.sxattribute.feature.source;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** 非持久化模式的空仓库；来源仍由 SourceService 内存管理。 */
public final class MemorySourceRepository implements SourceRepository {
    @Override
    public List<SourceRecord> load(UUID playerId) {
        return Collections.emptyList();
    }

    @Override
    public boolean save(SourceRecord record, long expectedVersion) {
        return true;
    }

    @Override
    public boolean delete(UUID playerId, String source, long expectedVersion) {
        return true;
    }

    @Override
    public void close() {
    }
}
