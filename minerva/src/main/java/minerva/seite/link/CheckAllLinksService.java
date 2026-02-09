package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class CheckAllLinksService {
    private final BookSO book;
    private final List<String> langs;

    public CheckAllLinksService(BookSO book, List<String> langs) {
        this.book = book;
        this.langs = langs;
    }

    public List<CALBrokenLink> getBrokenLinks() {
        book.getUser()._onlyAdmin();
        List<CALBrokenLink> ret = new ArrayList<>();
        for (SeiteSO seite : book.getAlleSeiten()) {
            InvalidLinksModel invalidLinks = new InvalidLinksModel(seite, langs);
            for (Link link : invalidLinks.getLinks()) {
                ret.add(new CALBrokenLink(link, seite));
            }
        }
        return ret;
    }
}
