package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.pmw.tinylog.Logger;

import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class LinkService {
    private static final String x1 = "href=\"";

    private LinkService() {
    }
    
    public static List<Link> extractInvalidLinks(String html, List<BookSO> books) {
        return extractLinks(html, true).stream()
                .filter(link -> !validLink(link.getHref(), books))
                .collect(Collectors.toList());
    }
    
    public static List<Link> extractLinks(String html, boolean withTitle) {
        List<Link> links = new ArrayList<>();
        int o = html.indexOf(x1);
        while (o >= 0) {
            o += x1.length();
            int oo = html.indexOf("\"", o);
            if (oo > o) {
                String href = html.substring(o, oo);
                Link link = new Link();
                link.setHref(href);
                if (withTitle) {
                    int u = html.indexOf("</a>", oo);
                    if (u > oo) {
                        int uu = html.indexOf(">", oo);
                        link.setTitle(plainText(html.substring(uu + 1, u)));
                    }
                }
                if (!contains(link, links)) {
                    links.add(link);
                }
            }
            o = html.indexOf(x1, o);
        }
        return links;
    }
    
    private static boolean contains(Link x, List<Link> links) {
        for (Link i : links) {
            if (i.getHref().equals(x.getHref()) && i.getTitle().equals(x.getTitle())) {
                return true;
            }
        }
        return false;
    }

    private static String plainText(String title) {
        try {
            return Jsoup.parse(title).getElementsByTag("body").text();
        } catch (Exception e) {
            Logger.error(e);
            return title;
        }
    }

    private static boolean validLink(String href, List<BookSO> books) {
        if (href.isBlank() || href.startsWith("http://") || href.startsWith("https://") || href.startsWith("#") || href.contains("/help-keys/")) {
            return true;
        }
        for (BookSO book : books) {
            SeiteSO seite = book._seiteById(href);
            if (seite != null) {
                return true;
            }
        }
        return false;
    }

    public static String replace(String html, List<Link> links) {
        for (Link link : links) {
            html = html.replace(x1 + link.getHref() + "\"", x1 + link.getSeiteId() + "\"");
        }
        return html;
    }
}
