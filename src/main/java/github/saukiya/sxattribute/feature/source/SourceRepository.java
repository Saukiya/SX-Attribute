package github.saukiya.sxattribute.feature.source;

import java.util.List;
import java.util.UUID;

/**
 * 属性源事实存储接口。实现必须以版本号进行乐观并发控制。
 */
public interface SourceRepository extends AutoCloseable {

    List<SourceRecord> load(UUID playerId) throws Exception;

    boolean save(SourceRecord record, long expectedVersion) throws Exception;

    boolean delete(UUID playerId, String source, long expectedVersion) throws Exception;

    @Override
    void close();
}
