package minerva.base;

import static github.soltaufintel.amalia.web.action.Escaper.esc;
import static github.soltaufintel.amalia.web.action.Escaper.urlEncode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import gitper.base.StringService;
import minerva.model.StateSO;

/**
 * Thread- and overflow-safe map
 */
public class Tosmap {
    private static final Map<String, TosmapEntry> map = new HashMap<>();
	private static final Object LOCK = new Object();
    
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
        
        synchronized (LOCK) {
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
            MinervaMetrics.TOSMAP_SIZE.set(map.size());
        }
    }
    
    public static Object get(String key) {
        if (StringService.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key must not be empty");
        }

        final long time = System.currentTimeMillis();
        synchronized (LOCK) {
            TosmapEntry ret = map.get(key);
            if (ret == null) {
                return null;
            }
            if (time > ret.getExpires()) {
                String info = "removed from Tosmap by get(): " + key + " / " + (ret.getData() == null ? "null"
                        : ret.getData().getClass().getSimpleName() + " / " + time + ">" + ret.getExpires());
                map.remove(key);
                MinervaMetrics.TOSMAP_SIZE.set(map.size());
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
        synchronized (LOCK) {
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
        MinervaMetrics.TOSMAP_SIZE.set(map.size());
        return ret;
    }

    public static void remove(String key) {
        if (StringService.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key must not be empty");
        }

        synchronized (LOCK) {
            map.remove(key);
            MinervaMetrics.TOSMAP_SIZE.set(map.size());
        }
    }
    
    public static List<Object> getValues() {
        synchronized (LOCK) {
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

	public static String getInfo() {
		String ret = "";
		for (Entry<String, TosmapEntry> e : map.entrySet()) {
			TosmapEntry v = e.getValue();
			ret += "\n- " + esc(e.getKey()) + " <a href=\"/tosmap?key=" + urlEncode(e.getKey(), "")
					+ "\" class=\"btn btn-xs btn-danger\">remove</a>: expires " + formatMillis(v.getExpires()) + " ("
					+ ((v.getExpires() - System.currentTimeMillis()) / 1000 / 60) + " minutes) -> "
					+ v.getData().getClass().getSimpleName() + ": ";
			if (v.getData() instanceof StateSO st) {
				ret += esc(st.getUser().getLogin());
			} else if (v.getData() != null) {
				ret += esc(v.getData().toString());
			} else {
				ret += "null";
			}
			ret += "\n";
		}
		return ret;
	}
	
	private static String formatMillis(long milliseconds) {
		Instant instant = Instant.ofEpochMilli(milliseconds);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Berlin"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		return localDateTime.format(formatter);
    }
}
