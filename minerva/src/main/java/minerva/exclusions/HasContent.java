package minerva.exclusions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class HasContent {
    private final SeiteSO seite;
    
    public HasContent(SeiteSO seite) {
        this.seite = seite;
    }

    /**
     * has content: > 0, has no content: 0
     * @return 1: page is not empty,
     * 2: page is empty, but at least one subpage is not empty,
     * 3: error (which should be interpreted as "page is not empty" to be on the safe side),
     * 0: page and subpages are empty.
     */
    public int hasContent(String lang) {
        if (seite.getBook().isFeatureTree()) {
            return 1;
        } else if (seite.getSeite().getTags().contains("autolink")) {
            return 0;
        }
        return hasContentR(lang);
    }
    
    public int hasContentR(String lang) {
        // In theory, this approach is a bit expensive since all content must be loaded and must be parsed.
        // However in practice it takes less than 0.4 seconds on the first call.
        try {
            String html = seite.getContent().getString(lang);
            Document doc = Jsoup.parse(html);
            Elements body = doc.select("body");
            if (body != null && !body.isEmpty() && body.get(0).childrenSize() > 0) {
                return 1;
            }
            for (SeiteSO sub : seite.getSeiten()) {
                if (sub.hasContent(lang) > 0) {
                    return 2;
                }
            }
            return 0;
        } catch (Exception e) {
            Logger.error(e);
            return 3;
        }
    }
    
    // ------------
    
    public SeiteVisible isVisible(String customer, String lang) {
        ExclusionsService sv = new ExclusionsService();
        sv.setCustomer(customer);
        sv.setExclusions(new Exclusions(seite.getBook().getWorkspace().getExclusions().get()));
        return isVisible(sv, lang);
    }
    
    public SeiteVisible isVisible(ExclusionsService sv, String lang) {
        return new SeiteVisible(isSeiteVisible(sv, lang), sv);
    }
    
    /**
     * Don't show page if return value is below 1.
     * @param seite page
     * @return 0: has no content, -1: not accessible, -2: not visible, 1, 2 or 3: show page (see SeiteSO.hasContent)
     */
    private int isSeiteVisible(ExclusionsService exclusionsService, String lang) {
        int c = hasContent(lang);
        if (c > 0) {
            exclusionsService.setSeite(seite);
            if (!exclusionsService.isAccessible()) {
                return -1;
            } else if (seite.getSeite().getTags().contains("invisible")) {
                return -2;
            }
        }
        return c;
    }
    
    // --------------
    
    public static boolean hasBookContent(BookSO book, String lang, ExclusionsService sv) {
        for (SeiteSO seite : book.getSeiten()) {
            if (new HasContent(seite).hasContent(lang) > 0 && sv.isAccessible(seite.getSeite().getTags())) {
                return true;
            }
        }
        return false;
    }
}
