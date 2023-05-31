package minerva.base;

import org.jsoup.Jsoup;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class StringService {

    private StringService() {
    }
    
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isBlank();
    }
    
    public static String prettyHTML(String html) {
        // https://mkyong.com/java/java-pretty-print-html/
        try {
            return Jsoup.parse(html).toString();
        } catch (Exception e) {
            Logger.error(e);
            return html;
        }
    }
    
    public static String prettyJSON(String json) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement je = JsonParser.parseString(json);
            return gson.toJson(je);
        } catch (JsonSyntaxException e) {
            Logger.error(e);
            return json;
        }
    }

    public static <T> String prettyJSON(T data) {
        return prettyJSON(new Gson().toJson(data));
    }
}
