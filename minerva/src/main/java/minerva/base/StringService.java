package minerva.base;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    
    public static String umlaute(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss");
    }

    public static List<String> upper(List<String> list) {
        return list.stream().map(i -> i.toUpperCase()).collect(Collectors.toList());
    }

    /**
     * @param text contains many Markdown links: (title)[url]
     * @return HTML link
     */
    public static String makeClickableLinks(String text) {
		Pattern regex = Pattern.compile("\\((.*)\\)\\[(.*)\\]", Pattern.MULTILINE);
		Matcher matcher = regex.matcher(text);
		while (matcher.find()) {
			String url = matcher.group(2);
			if (url.contains("createpage.action")) {
				continue;
			}
			String target = "";
			if (url.startsWith("http://") || url.startsWith("https://")) {
				target = " target=\"_blank\"";
			} else {
				url = "../" + url;
			}
			text = text.replace(matcher.group(0), "<a href=\"" + url + "\"" + target + ">" + matcher.group(1) + "</a>");
		}
		return text;
	}
}
