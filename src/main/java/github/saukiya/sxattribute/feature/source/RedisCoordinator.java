package github.saukiya.sxattribute.feature.source;

import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Redis 分布式锁与失效广播适配器。
 * <p>
 * 解锁使用 token 校验 Lua，避免锁过期后误删其它服务器新获得的锁。
 */
public final class RedisCoordinator implements AutoCloseable {

    private final JedisPool pool;
    private final String channel;
    private final long lockMillis;
    private volatile JedisPubSub subscription;

    public RedisCoordinator(ConfigurationSection config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(4);
        String password = config.getString("Password", "");
        pool = new JedisPool(poolConfig, config.getString("Host", "127.0.0.1"), config.getInt("Port", 6379),
                config.getInt("TimeoutMillis", 2000), password == null || password.isEmpty() ? null : password,
                config.getInt("Database", 0));
        channel = config.getString("Channel", "sxattribute:source:invalidate");
        lockMillis = config.getLong("LockMillis", 5000L);
    }

    public boolean available() {
        try (Jedis jedis = pool.getResource()) {
            return "PONG".equalsIgnoreCase(jedis.ping());
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public Lock acquire(UUID playerId, String source) {
        String key = "sxattribute:lock:" + playerId + ":" + source;
        String token = UUID.randomUUID().toString();
        // 使用 Lua 保证 NX 与毫秒过期时间原子生效，同时避开旧服务端内置 Jedis 对 set 参数重载的类路径污染。
        String script = "if redis.call('exists',KEYS[1]) == 0 then "
                + "redis.call('psetex',KEYS[1],ARGV[2],ARGV[1]); return 'OK' else return nil end";
        try (Jedis jedis = pool.getResource()) {
            Object result = jedis.eval(script, Collections.singletonList(key),
                    java.util.Arrays.asList(token, String.valueOf(lockMillis)));
            return "OK".equals(result) ? new Lock(key, token) : null;
        }
    }

    public void release(Lock lock) {
        if (lock == null) return;
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        try (Jedis jedis = pool.getResource()) {
            jedis.eval(script, Collections.singletonList(lock.key), Collections.singletonList(lock.token));
        }
    }

    public void publish(UUID playerId, String source) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, playerId + "|" + source);
        }
    }

    public void subscribe(Consumer<String> consumer) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                subscription = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        consumer.accept(message);
                    }
                };
                jedis.subscribe(subscription, channel);
            } catch (RuntimeException ignored) {
                // 断线时写入已由 available() 拒绝；下次重载会重新建立订阅。
            }
        }, "SXAttribute-Redis-Subscriber");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() {
        if (subscription != null) subscription.unsubscribe();
        pool.close();
    }

    public static final class Lock {
        private final String key;
        private final String token;

        private Lock(String key, String token) {
            this.key = key;
            this.token = token;
        }
    }
}
