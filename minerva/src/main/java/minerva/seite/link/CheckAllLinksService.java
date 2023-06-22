package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class CheckAllLinksService {
    private final BookSO book;
    private final List<String> langs;

    public CheckAllLinksService(BookSO book, List<String> langs) {
        this.book = book;
        this.langs = langs;
    }

    public List<CALBrokenLink> getBrokenLinks() {
        List<CALBrokenLink> ret=new ArrayList<>();
        check(book.getSeiten(), ret);
        return ret;
    }

    private void check(SeitenSO seiten, List<CALBrokenLink> links) {
        for (SeiteSO seite:seiten) {
            check(seite, links);
            check(seite.getSeiten(),links);
        }
    }

    private void check(SeiteSO seite, List<CALBrokenLink> links) {
        InvalidLinksModel invalidLinks = new InvalidLinksModel(seite, langs);
        for (Link link : invalidLinks.getLinks()) {
            links.add(new CALBrokenLink(link, seite));
        }
    }
}
