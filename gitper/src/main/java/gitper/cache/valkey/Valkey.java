package gitper.cache.valkey;

import java.util.Map;
import java.util.Map.Entry;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import github.soltaufintel.amalia.web.config.AppConfig;

/**
 * Minerva internal interface to "Valkey", the in-memory key-value store.
 */
public class Valkey {
    private static final ValkeyPool valkeyPool;
    private final String prefix;
    
    static {
        String host = null;
        try {
            host = new AppConfig().get("valkey");
        } catch (Exception e) {
        }
        if (host == null || host.isBlank()) {
            host = "docker05:6401";
            Logger.warn("Valkey host not set and set to fixed address: " + host);
        }
        String[] w = host.split(":");
        valkeyPool = new ValkeyPool(w[0], Integer.parseInt(w[1]));
    }

    public Valkey() {
        this.prefix = "";
    }

    public Valkey(String prefix) {
        this.prefix = prefix + "/";
    }

    public void put(String key, String value) {
        try (ValkeyResource valkey = valkeyPool.getResource()) {
            valkey.set(prefix + key, value);
        }
    }
    
    public void putAll(Map<String, String> map) {
        try (ValkeyResource valkey = valkeyPool.getResource()) {
            for (Entry<String, String> e : map.entrySet()) {
                valkey.set(prefix + e.getKey(), e.getValue());
            }
        }
    }

    public String get(String key) {
        try (ValkeyResource valkey = valkeyPool.getResource()) {
            return valkey.get(prefix + key);
        }
    }
    
    public <T> T getJson(String key, Class<T> cls) {
        return new Gson().fromJson(get(key), cls);
    }
    
    public <T> void putJson(String key, T data) {
        put(key, new Gson().toJson(data));
    }
    
    /**
     * Delete all keys starting with prefix
     */
    public void clear() {
        if (prefix.isEmpty()) {
            throw new RuntimeException("Valkey.clear() not allowed for empty prefix!");
        }
        try (ValkeyResource valkey = valkeyPool.getResource()) {
            for (String key : valkey.keys(prefix + "*")) {
                valkey.del(key);
            }
        }
    }
    
    /**
     * @param key full key, prefix is not prepended
     */
    public void delete(String key) {
        try (ValkeyResource valkey = valkeyPool.getResource()) {
            valkey.del(key);
        }
    }
    
    public long size() {
        try (ValkeyResource valkey = valkeyPool.getResource()) {
            return valkey.keys(prefix + "*").size();
        }
    }
}
