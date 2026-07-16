package github.saukiya.sxattribute.feature.source;

/** 属性源写入结果，API 调用方可区分配置、锁和存储故障。 */
public enum SourceWriteResult {
    APPLIED,
    REMOVED,
    UNIQUE_EXISTS,
    REDIS_UNAVAILABLE,
    LOCK_CONFLICT,
    STORAGE_DISABLED,
    VERSION_CONFLICT,
    FAILED
}
