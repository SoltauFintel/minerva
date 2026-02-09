package minerva.seite.link;

import java.util.List;

import com.github.template72.data.DataList;

import minerva.book.BPage;

public class CheckAllLinksPage extends BPage {

    @Override
    protected void execute() {
        onlyAdmin();
        
        List<CALBrokenLink> links = new CheckAllLinksService(book, langs).getBrokenLinks();

        header(n("checkAllLinks"));
        put("bookTitle", esc(book.getTitle()));
        DataList list = list("links");
        for (CALBrokenLink link : links) {
            list.add().put("text", n("checkLink")
                    .replace("$h", esc(link.getLink().getHref()))
                    .replace("$v", esc("/s/" + branch + "/" + bookFolder + "/" + link.getSeite().getId()))
                    .replace("$t", esc(link.getSeite().getTitle()))
                    .replace("$l", esc(link.getLink().getTitle())));
        }
        put("hasBL", !list.isEmpty());
    }
}
