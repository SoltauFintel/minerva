package gitper.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import gitper.cache.valkey.ValkeyCacheBuilder;

/**
 * Creates by default a transient in-memory cache.
 * Use valkey() for creating a quite persistent in-memory cache.
 */
public class CacheBuilder<C> {
    private static final Object LOCK = new Object();
    private static final Map<String, Cache<String, ?>> map = new HashMap<>();
    private int minutes = 20;

    // no effect for Valkey
    public CacheBuilder<C> minutes(int minutes) {
        this.minutes = minutes;
        return this;
    }

    public ValkeyCacheBuilder<C> valkey() {
        return new ValkeyCacheBuilder<C>();
    }

    public C build(String cacheTypeName, String key, Function<String, C> func) {
        synchronized (LOCK) {
            @SuppressWarnings("unchecked")
            Cache<String, C> caffeine = (Cache<String, C>) map.get(cacheTypeName);
            if (caffeine == null) {
                caffeine = Caffeine.newBuilder() //
                        .expireAfterWrite(Duration.ofMinutes(minutes)) //
                        .build();
                map.put(cacheTypeName, caffeine);
            }
            return caffeine.get(key, func); // liefert Cache Klasse
        }
    }

    public C build(String cacheTypeName, Function<String, C> func) {
        return build(cacheTypeName, "CACHE", func);
    }
    
    public static void remove(String cacheTypeName) {
        synchronized (LOCK) {
            map.remove(cacheTypeName);
        }
    }
}
