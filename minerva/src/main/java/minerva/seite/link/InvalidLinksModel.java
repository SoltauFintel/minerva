package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import minerva.git.CommitMessage;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

/**
 * Invalid links for LinksResolverPage
 */
public class InvalidLinksModel {
    private final List<Link> links = new ArrayList<>();

    public InvalidLinksModel(SeiteSO seite, List<String> langs) {
        List<BookSO> books = new ArrayList<>();
        books.add(seite.getBook());
        for (String lang : langs) {
            links.addAll(LinkService.extractInvalidLinks(seite.getContent().getString(lang), books));
        }
    }

    /**
     * @return invalid links, no duplicates, with titles, all languages
     */
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
            String neu = LinkService.replace(html, links);
            if (!html.equals(neu)) {
                seite.getContent().setString(lang, neu);
                dirty = true;
            }
        }
        if (dirty) {
            seite.saveHtml(new CommitMessage(seite, "links replaced"), langs);
        }
    }
}
