package github.saukiya.sxattribute.feature.source;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/** 持久化层使用的可移植来源记录，不依赖 Bukkit 实体对象。 */
@AllArgsConstructor
@Getter
public final class SourceRecord {
    private final UUID playerId;
    private final String source;
    private final List<String> attributes;
    private final int stacks;
    private final long expiresAt;
    private final long version;
    private final String updatedBy;
    private final List<String> tags;
}
