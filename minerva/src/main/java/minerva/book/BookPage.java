package minerva.book;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.base.DeliverHtmlContent;
import minerva.base.Uptodatecheck;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.PageTree;
import minerva.seite.ViewSeitePage;

public class BookPage extends BPage implements Uptodatecheck {
    public static DeliverHtmlContent<BookSO> additionalButtons = i -> "";
    public static DeliverHtmlContent<BookSO> additionalFeatureTreeButtons = i -> "";
    
    @Override
    protected void execute() {
        boolean allPages = user.getUser().isShowAllPages();
        String guiLanguage = user.getGuiLanguage();
        String pageLanguage = user.getPageLanguage();
        if (book.isFeatureTree()) {
            if (!"de".equals(pageLanguage)) {
                user.selectPageLanguage("de");
                render = false;
                ctx.redirect(bookFolder);
                return;
            }
            Logger.info(user.getLogin() + " | " + book.getWorkspace().getBranch() + " | " + book.getTitle());
        }
        
        setJQueryObenPageMode();
        String title = book.getBook().getTitle().getString(guiLanguage);
        put("header", esc(title));
        put("title", esc(title.toLowerCase().contains("buch") ? title : title + " (Buch)"));
        put("allPages", allPages);
        put("hasLeftArea", true);
        put("leftAreaContent", new PageTree().getHTML(book.getSeiten(), langs, null, pageLanguage));
        if (isOneLang()) {
            langs = oneLang(model, book);
        }
        put("isFeatureTree", book.isFeatureTree());
        put("positionlink", booklink + "/order");
        put("sortlink", booklink + "/sort");
        put("hasPositionlink", book.getSeiten().size() > 1);
        boolean sorted = book.getBook().isSorted();
        put("isSorted", sorted);
        put("Sortierung", n(sorted ? "alfaSorted" : "manuSorted"));
        put("newPage", n(book.isFeatureTree() ? "newFeature" : "newPage"));
        put("additionalButtons", additionalButtons.getHTML(book));
        put("additionalFeatureTreeButtons", additionalFeatureTreeButtons.getHTML(book));
        put("hasPrevlink", false);
        boolean hasSeiten = !book.getSeiten().isEmpty();
        put("hasNextlink", hasSeiten);
        if (hasSeiten) {
            put("nextlink", "/s/" + branch + "/" + book.getBook().getFolder() + "/" + book.getSeiten().get(0).getId());
        }
        SeiteSO change = book.getLastChange();
        put("hasLastChange", change != null);
        if (change != null) {
            ViewSeitePage.fillLastChange(change, change.getLastChange(), n("lastChangeInfoForBook")/*no esc()*/, model);
        }

        DataList list = list("languages");
        for (String lang : langs) {
            DataMap map = list.add();
            map.put("lang", lang);
            map.put("LANG", lang.toUpperCase());
            map.put("gliederung", new Gliederung(book, lang, allPages).build());
            map.put("active", pageLanguage.equals(lang));
            String bookTitle = book.getBook().getTitle().getString(lang);
            if (bookTitle.isBlank()) {
                bookTitle = book.getBook().getFolder();
            }
            map.put("bookTitle", esc(bookTitle));
        }
    }
    
    public static List<String> oneLang(DataMap model, BookSO book) {
        model.put("hasBook", false);
        model.put("hasMenuItems", true);
        DataList menuItems = model.list("menuItems");
        DataMap map = menuItems.add();
        map.put("link", "/b/" + book.getWorkspace().getBranch() + "/" + book.getBook().getFolder());
        map.put("title", Escaper.esc(book.getTitle()));

        return MinervaWebapp.factory().getConfig().getOneLang();
    }
}
