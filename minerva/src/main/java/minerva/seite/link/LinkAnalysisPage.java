package minerva.seite.link;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class LinkAnalysisPage extends SPage {

    @Override
    protected void execute() {
        header(n("linkAnalysis"));
        DataList list = list("links");
        // outgoing links
        String linkPrefix = "/s/" + branch + "/" + bookFolder + "/";
        for (String lang : langs) {
            List<Link> links = LinkService.extractLinks(seite.getContent().getString(lang), true);
            for (Link link : links) {
                DataMap map = list.add();
                boolean external = link.getHref().startsWith("http://") || link.getHref().startsWith("https://");
                map.put("internal", !external);
                map.put("href", esc(external ? link.getHref() : linkPrefix + link.getHref()));
                map.put("id", esc(link.getSeiteId()));
                map.put("linkTitle", esc(link.getTitle())); // This is the link title.
                map.put("pageTitle", esc(getPageTitle(link, external, lang)));
                map.put("lang", lang.toUpperCase());
                map.put("outgoing", true);
            }
        }
        // incoming links
        analyze(book, list);
        put("hasLinks", !list.isEmpty());
    }

    private String getPageTitle(Link link, boolean external, String lang) {
        if (external) {
            return "";
        }
        SeiteSO s = book._seiteById(link.getHref());
        return s == null ? link.getTitle() : s.getSeite().getTitle().getString(lang);
    }

    private void analyze(BookSO book, DataList list) {
        for (SeiteSO s : book.getAlleSeiten()) {
            for (String lang : langs) {
                List<Link> links = LinkService.extractLinks(s.getContent().getString(lang), true);
                for (Link link : links) {
                    if (link.getHref().equals(seite.getId())) {
                        DataMap map = list.add();
                        map.put("internal", !(link.getHref().startsWith("http://") || link.getHref().startsWith("https://")));
                        map.put("href", esc("/s/" + branch + "/" + bookFolder + "/" + s.getId()));
                        map.put("id", esc(s.getId()));
                        map.put("linkTitle", esc(link.getTitle()));
                        map.put("pageTitle", esc(s.getTitle()));
                        map.put("lang", lang.toUpperCase());
                        map.put("outgoing", false);
                    }
                }
            }
        }
    }
}
