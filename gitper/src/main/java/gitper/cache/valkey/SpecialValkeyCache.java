package gitper.cache.valkey;

import java.util.Map;

import gitper.base.StringService;

public abstract class SpecialValkeyCache {
    private final Valkey valkey;
    /** Valkey key, der "1" liefert wenn Cache aufgebaut worden ist */
    private final String cacheSetUpName;

    public SpecialValkeyCache(String prefix, String cacheSetUpName) {
        valkey = new Valkey(prefix);
        this.cacheSetUpName = cacheSetUpName;
    }

    public String get(String key) {
        String ret = valkey.get(key);
        if (StringService.isNullOrEmpty(ret) && cacheSetUpName != null) {
            if ("1".equals(valkey.get(cacheSetUpName))) { // Wurde Cache überhaupt schon aufgebaut?
                ret = loadOne(key); // Item ist noch nicht im Cache, da Item ganz neu.
                if (!StringService.isNullOrEmpty(ret)) {
                    valkey.put(key, ret);
                }
            } else {
                build();
                ret = valkey.get(key);
            }
        }
        return ret;
    }

    public void build() {
        valkey.putAll(loadAll());
        valkey.put(cacheSetUpName, "1");
    }

    protected abstract String loadOne(String key);

    protected abstract Map<String, String> loadAll();
}
