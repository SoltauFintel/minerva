package gitper.cache.valkey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// instantiiert durch CacheBuilder<C>
public class ValkeyCacheBuilder<C> {
    private static final Object LOCK = new Object();
    private static final Map<String, Valkey> valkeys = new HashMap<>();
    private String prefix = "";
    
    public ValkeyCacheBuilder<C> prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public C build(String cacheTypeName, Function<Valkey, C> func) {
        cacheTypeName += ":" + prefix;
        Valkey valkey = valkeys.get(cacheTypeName);
        if (valkey == null) {
            valkey = new Valkey(prefix);
            valkeys.put(cacheTypeName, valkey);
        }
        Function<Valkey, C> wrapper = new Function<>() {
            @Override
            public C apply(Valkey t) {
                synchronized (LOCK) {
                    return func.apply(t);
                }
            }
        };
        return wrapper.apply(valkey);
    }
}
