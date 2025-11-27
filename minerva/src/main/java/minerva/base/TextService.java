package minerva.base;

import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.pmw.tinylog.Logger;

import ohhtml.Thumbnails;

// ~StringService
public class TextService {

    private TextService() {
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
    
    /**
     * @param html the whole HTML string
     * @param tag tag name
     * @param attr attribute name
     * @return set of attribute values
     */
    public static Set<String> findHtmlTags(String html, String tag, String attr) {
        return Thumbnails.findHtmlTags(html, tag, attr, all -> true, false);
    }

    public static String findCopyOfTitle(String title, String lang, List<String> titles) {
        final int limit = 100;
        
        // remove all "copy [n] of " prefixes
        boolean found = true;
        while (found) {
            found = false;
            String prefix = NLS.get(lang, "copy-of") + " ";
            while (title.startsWith(prefix)) {
                found = true;
                title = title.substring(prefix.length());
            }
            for (int i = 2; i < limit; i++) {
                prefix = NLS.get(lang, "copy-n-of").replace("$n", "" + i) + " ";
                while (title.startsWith(prefix)) {
                    found = true;
                    title = title.substring(prefix.length());
                }
            }
        }

        // prepend copy of prefix
        String prefix = NLS.get(lang, "copy-of") + " ";
        String copyOf = prefix + title;
        if (titles.contains(copyOf)) {
            for (int i = 2; i < limit; i++) {
                prefix = NLS.get(lang, "copy-n-of").replace("$n", "" + i) + " ";
                String n = prefix + title;
                if (!titles.contains(n)) {
                    return n;
                }
            }
        }
        return copyOf; // "copy of" or limit exceeded (killer loop protection)
    }
}
