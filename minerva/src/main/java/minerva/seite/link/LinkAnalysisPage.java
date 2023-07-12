package minerva.seite.link;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.SPage;

public class LinkAnalysisPage extends SPage {

    @Override
    protected void execute() {
        header(n("linkAnalysis"));
        DataList list = list("links");
        // outgoing links
        for (String lang : langs) {
            List<Link> links = LinkService.extractLinks(seite.getContent().getString(lang), true);
            for (Link link : links) {
                DataMap map = list.add();
                map.put("internal", !(link.getHref().startsWith("http://") || link.getHref().startsWith("https://")));
                map.put("href",
                        esc(link.getHref().startsWith("http://") || link.getHref().startsWith("https://")
                                ? link.getHref()
                                : "/s/" + branch + "/" + bookFolder + "/" + link.getHref()));
                map.put("id", esc(link.getSeiteId()));
                map.put("title", esc(link.getTitle())); // This is the link title.
                // TODO page title
                map.put("lang", lang);
                map.put("outgoing", true);
            }
        }
        // incoming links
        analyze(book.getSeiten(), list);
    }

    private void analyze(SeitenSO seiten, DataList list) {
        for (SeiteSO s : seiten) {
            for (String lang : langs) {
                List<Link> links = LinkService.extractLinks(s.getContent().getString(lang), true);
                for (Link link : links) {
                    if (link.getHref().equals(seite.getId())) {
                        DataMap map = list.add();
                        map.put("internal", !(link.getHref().startsWith("http://") || link.getHref().startsWith("https://")));
                        map.put("href",
                                esc(link.getHref().startsWith("http://") || link.getHref().startsWith("https://")
                                        ? link.getHref()
                                        : "/s/" + branch + "/" + bookFolder + "/" + link.getHref()));
                        map.put("id", esc(s.getId()));
                        // link.title is the link title.
                        map.put("title", esc(s.getTitle())); // page title
                        map.put("lang", lang);
                        map.put("outgoing", false);
                    }
                }
            }
        }
    }
}
