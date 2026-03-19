package gitper.cache;

import java.util.function.Function;

/**
 * Simple String/String cache: key and value are Strings
 */
public abstract class StringCache implements Function<String, String> {
    private final int expiresInMinutes;

    public StringCache() {
        this(20);
    }

    public StringCache(int expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }
    
    protected String getCacheTypeName() {
        return getClass().getSimpleName();
    }

    public String get(String key) {
        return new CacheBuilder<String>().minutes(expiresInMinutes).build(getCacheTypeName(), key, this);
    }

    public void clear() {
        CacheBuilder.remove(getCacheTypeName());
    }

    /** teures Laden hier implementieren */
    @Override
    public abstract String apply(String key);
}
