package gitper.base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.web.action.Escaper;

public class StringService {

    private StringService() {
    }
    
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isBlank();
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
        Pattern regex = Pattern.compile("\\(([^\\)]+)\\)\\[([^\\]]+)\\]");
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
                if (url.startsWith("N")) { // comment link
                    url = url.substring(1);
                    url = "?highlight=" + url + "#" + url;
                } else { // page link
                    url = "../" + url;
                }
            }
            text = text.replace(matcher.group(0), "<a href=\"" + url + "\"" + target + ">" + matcher.group(1) + "</a>");
        }
        return text;
    }

    /**
     * @param html HTML with http/https links
     * @return HTML with clickable links
     */
    public static String makeClickableLinks2(String html) {
        return HtmlLinkifier.makeLinksClickable(html);
    }
    
    /**
     * Limit text to maxlen, but to do not cut text within link "(...)[...]"
     */
    public static String cutOutsideLinks(String text, int maxlen) {
        // TODO man sollte auch http:.... Links unterstuetzen
        if (text == null || maxlen < 1 || text.length() < maxlen) {
            return text;
        }
        String ret = "";
        boolean inside = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                int o = text.indexOf(")[", i);
                inside = o > i && text.indexOf("]", i) > o;
            }
            ret += c;
            if (i >= maxlen && !inside) {
                return ret;
            }
            if (inside && c == ']') {
                inside = false;
            }
        }
        return ret;
    }
    
    public static String today() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public static String onlyBody(String html) {
        if (html == null) {
            return "";
        }
        int o = html.indexOf("<body>");
        if (o >= 0) {
            String ret = html.substring(o + "<body>".length());
            o = ret.lastIndexOf("</body>");
            if (o >= 0) {
                return ret.substring(0, o);
            }
        }
        return html;
    }
    
    public static String unquote(String str) {
        return unquote(str, "\"", "\"");
    }

    public static String unquote(String str, String start, String end) {
        return str != null && str.startsWith(start) && str.endsWith(end) ? str.substring(start.length(), str.length() - end.length()) : str;
    }

    public static boolean onlyDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return str.length() > 0;
    }

    public static boolean isVersionNumber(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= '0' && c <= '9') {
                if (i == 0 && c == '0') {
                    return false;
                }
            } else if (c == '.') {
                if (i == 0 || str.charAt(i - 1) == '.' || i == str.length() - 1) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return str.length() > 0;
    }
    
    /**
     * @param a valid version number
     * @param b valid version number
     * @return a.equals(b) or 4.01.0 == 4.01
     */
    public static boolean equalVersions(String a, String b) {
        if (a == null || b == null) {
            return false;
        } else if (a.equals(b)) {
            return true;
        }
        return equalMitOhne0Versions(a, b) || equalMitOhne0Versions(b, a);
    }
    
    private static boolean equalMitOhne0Versions(String mit0, String ohne0) {
        return (ohne0 + ".0").equals(mit0);
    }
    
    public static String makeVersionSort(String version) {
        String ret = "";
        String[] w = version.split("\\.");
        for (int i = 0; i < w.length; i++) {
            String a = w[i];
            while (a.length() < 2) {
                a = "0" + a;
            }
            if (!ret.isEmpty()) {
                ret += ".";
            }
            ret += a;
        }
        return ret;
    }
    
    public static String makeTicketNumberSort(String ticketNumber) {
        int o = ticketNumber.indexOf("-");
        if (o < 1) {
            return ticketNumber;
        }
        String prefix = ticketNumber.substring(0, o + "-".length());
        String number = ticketNumber.substring(o + "-".length());
        if (onlyDigits(number)) {
            while (number.length() < 6) {
                number = "0" + number;
            }
        }
        return prefix + number;
    }

    public static boolean isWhitespace(String str, int position) {
        if (position >= 0 && position < str.length()) {
            char c = str.charAt(position);
            return (c == ' ' || c == '\t' || c == ',' || c == '\n');
        } else {
            return true;
        }
    }

    /**
     * @param str e.g. a tag
     * @return number of "." in str
     */
    public static int dots(String str) {
        int ret = 0;
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '.') {
                    ret++;
                }
            }
        }
        return ret;
    }

    public static String getParentVersion(String version) {
        int o = version.indexOf("-SNAPSHOT");
        if (o >= 0) {
            return version.substring(0, o);
        }
        return dots(version) > 1 ? Escaper.esc(version.substring(0, version.lastIndexOf("."))) : "master";
    }

    public static String seven(String commitId) {
        return commitId != null && commitId.length() > 7 ? commitId.substring(0, 7) : commitId;
    }
    
    public static String getFirstLine(String text) {
        if (text == null) {
            return "";
        }
        text = text.trim();
        while (text.startsWith("\n")) {
            text = text.substring(1).trim();
        }
        text = text.replace("\r\n", "\n");
        int o = text.indexOf("\n");
        return o >= 0 ? text.substring(0, o) : text;
    }
}
