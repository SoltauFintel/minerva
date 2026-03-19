package gitper.cache.valkey;

import java.io.Closeable;
import java.util.Set;

public final class ValkeyResource implements Closeable {
    private final io.valkey.Jedis r;

    ValkeyResource(io.valkey.Jedis resource) {
        this.r = resource;
    }

    public void set(String key, String value) {
        r.set(key, value);
    }

    public void del(String key) {
        r.del(key);
    }

    public String get(String key) {
        return r.get(key);
    }

    public Set<String> keys(String pattern) {
        return r.keys(pattern);
    }

    @Override
    public void close() {
        r.close();
    }
}
