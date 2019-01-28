package server.utilities;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

public class RedissonHelper {

    private static final RedissonClient redisson = Redisson.create();

    private static RBucket<Object> getRedisBucket(String key) {
        return redisson.getBucket(key);
    }

    public static Object getSet(final String key, final Object value) {
        RBucket<Object> rBucket = getRedisBucket(key);
        return rBucket.getAndSet(value);
    }

    public static void set(final String key, final Object value) {
        RBucket<Object> temp = getRedisBucket(key);
        temp.set(value);
    }

    public static Object get(final String key) {
        RBucket<Object> temp = getRedisBucket(key);
        return temp.get();
    }

    public static Boolean isExists(final String key) {
        RBucket<Object> temp = getRedisBucket(key);
        return temp.isExists();
    }
}
