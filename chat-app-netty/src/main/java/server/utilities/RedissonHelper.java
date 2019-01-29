package server.utilities;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

public class RedissonHelper {

    public static RedissonClient getRedisson() {
        return redisson;
    }

    private static final RedissonClient redisson = Redisson.create();

    // RBucket

    private static RBucket<Object> getRedisBucket(String key) {
        return redisson.getBucket(key);
    }

    public static Object getSet(final String key, final Object value) {
        RBucket<Object> rBucket = getRedisBucket(key);
        return rBucket.getAndSet(value);
    }

    public static void setRBucket(final String key, final Object value) {
        RBucket<Object> temp = getRedisBucket(key);
        temp.set(value);
    }

    public static Object getRBucket(final String key) {
        RBucket<Object> temp = getRedisBucket(key);
        return temp.get();
    }

    public static Boolean isExistsRBucket(final String key) {
        RBucket<Object> temp = getRedisBucket(key);
        return temp.isExists();
    }

    // RMap

    public static RMap<Object, Object> redisMap(String key) {
        return redisson.getMap(key);
    }

    public static RMap<Object, Object> createRedisMap(String key) {
        return redisMap(key);
    }

    public static RMap<Object, Object> getRedisMap(String key) {
        return redisMap(key);
    }

}
