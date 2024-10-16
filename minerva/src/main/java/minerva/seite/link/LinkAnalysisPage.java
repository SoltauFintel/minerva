package minerva.seite.link;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BookPage;
import minerva.seite.SPage;

public class LinkAnalysisPage extends SPage {

    @Override
    protected void execute() {
        List<LinkAnalysisEntry> result = new LinkAnalysisService(branch, bookFolder, seite, langs).get();
        
        header(n("linkAnalysis"));
        DataList list = list("links");
        for (LinkAnalysisEntry e : result) {
            DataMap map = list.add();
//            map.put("id", esc(e.getId()));
            map.put("href", esc(e.getHref()));
            map.put("linkTitle", esc(e.getLinkTitle())); // This is the link title.
            map.put("pageTitle", esc(e.getPageTitle()));
            map.put("lang", esc(e.getLang()));
            map.put("internal", e.isInternal());
            map.put("outgoing", e.isOutgoing());
            map.put("crossbook", e.isCrossBook());
        }
        put("hasLinks", !result.isEmpty());

        if (book.isNotPublic()) {
            BookPage.oneLang(model, book);
        }
    }
}
