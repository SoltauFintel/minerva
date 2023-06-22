package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.pmw.tinylog.Logger;

import minerva.git.CommitMessage;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class LinksModel {
    private static final String x1 = "href=\"";
    private final List<Link> links = new ArrayList<>();

    public LinksModel(SeiteSO seite, List<String> langs) {
        List<BookSO> books = new ArrayList<>();
        books.add(seite.getBook());
        // HTML nach Links durchsuchen
        // Entscheiden, ob Link so ok ist.
        for (String lang : langs) {
            links.addAll(find(seite.getContent().getString(lang), books));
        }
    }

    private List<Link> find(String html, List<BookSO> books) {
        List<Link> links = new ArrayList<>();
        int o = html.indexOf(x1);
        while (o >= 0) {
            o += x1.length();
            int oo = html.indexOf("\"", o);
            if (oo > o) {
                String href = html.substring(o, oo);
                if (!validLink(href, books)) {
                    Link link = new Link();
                    link.setHref(href);
                    int ooo = html.indexOf("</a>", oo);
                    if (ooo > oo) {
                        int oooo = html.indexOf(">", oo);
                        link.setTitle(plainText(html.substring(oooo + 1, ooo)));
                    }
                    boolean gef = false;
                    for (Link x : links) {
                        if (x.getHref().equals(link.getHref()) && x.getTitle().equals(link.getTitle())) {
                            gef = true;
                            break;
                        }
                    }
                    if (!gef) {
                        links.add(link);
                    }
                }
            }
            o = html.indexOf(x1, o);
        }
        return links;
    }

    private String plainText(String title) {
        try {
            return Jsoup.parse(title).getElementsByTag("body").text();
        } catch (Exception e) {
            Logger.error(e);
            return title;
        }
    }

    private boolean validLink(String href, List<BookSO> books) {
        if (href.isBlank() || href.startsWith("http://") || href.startsWith("https://") || href.startsWith("#")) {
            return true;
        }
        for (BookSO book : books) {
            SeiteSO seite = book.getSeiten()._byId(href);
            if (seite != null) {
                return true;
            }
        }
        return false;
    }

    public List<Link> getLinks() {
        return links;
    }

    public boolean hasLinks() {
        return !links.isEmpty();
    }

    public void save(SeiteSO seite, List<String> langs) {
        boolean dirty = false;
        for (String lang : langs) {
            String html = seite.getContent().getString(lang);
            String neu = replace(html);
            if (!html.equals(neu)) {
                seite.getContent().setString(lang, neu);
                dirty = true;
            }
        }
        if (dirty) {
            seite.saveHtml(new CommitMessage(seite, "links replaced"), langs);
        }
    }

    private String replace(String html) {
        for (Link link : links) {
            html = html.replace(x1 + link.getHref() + "\"", x1 + link.getSeiteId() + "\"");
        }
        return html;
    }
}
