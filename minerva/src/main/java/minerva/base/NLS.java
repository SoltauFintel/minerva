package minerva.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.github.template72.data.DataMap;

/**
 * National language support
 */
public class NLS {
    private static final Map<String, String> de = loadRB("de");
    private static final Map<String, String> en = loadRB("en");
    public static final DataMap dataMap_de = loadDataMap(de);
    public static final DataMap dataMap_en = loadDataMap(en);
    
    private NLS() {
    }

    private static Map<String, String> loadRB(String lang) {
        // Properties Klasse ist doof
        Map<String, String> map = new HashMap<>();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(NLS.class.getResourceAsStream("/" + lang + ".rb")))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                int o = line.indexOf("=");
                if (o >= 0) {
                    String key = line.substring(0, o).trim();
                    String value = line.substring(o + 1).trim();
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading NLS file '" + lang + "'", e);
        }
        return map;
    }

    private static DataMap loadDataMap(Map<String, String> map) {
        DataMap dataMap = new DataMap();
        dataMap.putAll(map);
        return dataMap;
    }

    public static Map<String, String> getProperties(String lang) {
        return "en".equals(lang) ? en : de;
    }
    
    public static String get(String lang, String key) {
        String text = getProperties(lang).get(key);
        return text == null ? key : text;
    }
}
