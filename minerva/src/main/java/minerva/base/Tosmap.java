package minerva.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

/**
 * Thread- and overflow-safe map
 */
public class Tosmap {
    private static final Map<String, TosmapEntry> map = new HashMap<>();
    private static final String HANDLE = "map";
    
    private Tosmap() {
    }
    
    /**
     * @param key not null and not empty
     * @param expires time in future in milliseconds
     * @param data can be null
     */
    public static void add(String key, long expires, Object data) {
        final long time = System.currentTimeMillis();
        if (StringService.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key must not be empty");
        } else if (expires < time) {
            throw new IllegalArgumentException("expires value too small\n" + expires + "<" + time);
        }
        
        synchronized (HANDLE) {
            // cleanup
            Iterator<Entry<String, TosmapEntry>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, TosmapEntry> entry = iter.next();

                if (time > entry.getValue().getExpires()) {
                    String info = "removed from Tosmap by add(): " + entry.getKey() + " / "
                            + (entry.getValue().getData() == null ? "null"
                                    : entry.getValue().getData().getClass().getSimpleName() + " / " + time + ">"
                                            + entry.getValue().getExpires());
                    iter.remove();
                    Logger.debug(info);
                }
            }
    
            // add
            map.put(key, new TosmapEntry(expires, data));
        }
    }
    
    public static Object get(String key) {
        if (StringService.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key must not be empty");
        }

        final long time = System.currentTimeMillis();
        synchronized (HANDLE) {
            TosmapEntry ret = map.get(key);
            if (ret == null) {
                return null;
            }
            if (time > ret.getExpires()) {
                String info = "removed from Tosmap by get(): " + key + " / " + (ret.getData() == null ? "null"
                        : ret.getData().getClass().getSimpleName() + " / " + time + ">" + ret.getExpires());
                map.remove(key);
                Logger.info(info);
                return null;
            }
            return ret.getData();
        }
    }
    
    /**
     * Search for keys using begin of key.
     * @param keyBegin begin of key
     * @return keys, can be outdated
     */
    public static List<String> search(String keyBegin) {
        if (StringService.isNullOrEmpty(keyBegin)) {
            throw new IllegalArgumentException("keyBegin must not be empty");
        }
        synchronized (HANDLE) {
            List<String> ret = new ArrayList<>();
            for (String key : map.keySet()) {
                if (key.startsWith(keyBegin)) {
                    ret.add(key);
                }
            }
            return ret;
        }
    }

    public static Object pop(String key) {
        Object ret = get(key);
        remove(key);
        return ret;
    }

    public static void remove(String key) {
        if (StringService.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key must not be empty");
        }

        synchronized (HANDLE) {
            map.remove(key);
        }
    }
    
    public static List<Object> getValues() {
        synchronized (HANDLE) {
            return map.values().stream().map(v -> v.getData()).collect(Collectors.toList());
        }
    }

    private static class TosmapEntry {
        private final long expires;
        private final Object data;

        TosmapEntry(long expires, Object data) {
            this.expires = expires;
            this.data = data;
        }

        public long getExpires() {
            return expires;
        }

        public Object getData() {
            return data;
        }
    }
}
