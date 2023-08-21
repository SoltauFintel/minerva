package minerva.base;

import java.util.HashMap;
import java.util.Map;

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
}
