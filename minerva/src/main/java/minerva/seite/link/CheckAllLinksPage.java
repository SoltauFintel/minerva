package minerva.seite.link;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;

public class CheckAllLinksPage extends BPage {

    @Override
    protected void execute() {
        user.onlyAdmin();
        
        List<CALBrokenLink> links = new CheckAllLinksService(book, langs).getBrokenLinks();
        DataList list = list("links");
        for (CALBrokenLink link : links) {
            DataMap map = list.add();
            map.put("href", esc(link.getLink().getHref()));
            map.put("aTitle", esc(link.getLink().getTitle()));
            map.put("pageTitle", esc(link.getSeite().getTitle()));
            map.put("viewlink", esc("/s/" + branch + "/" + bookFolder + "/" + link.getSeite().getId()));
        }
        header("Check all links");
        put("bookTitle", esc(book.getTitle()));
    }
}
