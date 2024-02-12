package minerva.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Holds texts for different languages
 */
public class NlsString {
    private final Map<String, String> nls = new HashMap<>();
    
    public String getString(String language) {
        String ret = nls.get(language);
        return ret == null ? "" : ret;
    }
    
    public void setString(String language, String text) {
        nls.put(language, text);
    }

    public void from(NlsString title) {
        nls.clear();
        nls.putAll(title.nls);
    }
    
    public boolean contains(String q) {
        try {
            q = q.toLowerCase();
            for (String t : nls.values()) {
                if (t.toLowerCase().contains(q)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void putTo(String prefix, Map<String, String> target) {
        for (Entry<String, String> entry : nls.entrySet()) {
            target.put(prefix + entry.getKey(), entry.getValue());
        }
    }
}
