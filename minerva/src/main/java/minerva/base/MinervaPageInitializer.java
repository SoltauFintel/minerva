package minerva.base;

import static github.soltaufintel.amalia.web.action.Escaper.esc;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import github.soltaufintel.amalia.web.action.PageInitializer;
import minerva.MinervaWebapp;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.SeiteSO;

public class MinervaPageInitializer extends PageInitializer {
    
    @Override
    public void initPage(Context ctx, Page page) {
        MinervaPageInitModel m = new MinervaPageInitModel(ctx);
        page.put("title", "Minerva");
        page.put("abmelden", "Abmelden");
        page.put("hasUser", m.hasUser());
        page.put("VERSION", MinervaWebapp.VERSION);
        page.put("user", esc(m.getLogin()));
        page.put("gitlab", MinervaWebapp.factory().getConfig().isGitlab());
        page.put("booksLabel", "Bücher");
        page.put("searchPlaceholder", "");
        page.put("searchFocus", false);
        page.put("branch0", "");
        page.put("previewTitle", "Preview");
        page.put("previewlink", "/p/master");
        page.put("q", "");
        booksForMenu(m.hasUser(), m.getUserLang(), m.getBooks(), page);
        page.put("isCustomerVersion", MinervaWebapp.factory().isCustomerVersion());
        page.put("branch", esc(m.getBranch()));
        page.put("exclusionsTitle", "Exclusions");
        page.put("hasBook", false);
        page.put("book0Title", "");
        page.put("myTasks", "");
        hasUserVars(page, m);
    }

    private void booksForMenu(boolean hasUser, String userLang, BooksSO books, Page page) {
        DataList list = page.list("booksForMenu");
        page.put("bookslinkForMenu", "/w");
        if (!hasUser) {
            return;
        }
        if (books != null) {
            for (BookSO book : books) {
                DataMap map = list.add();
                map.put("folder", esc(book.getBook().getFolder()));
                map.put("title", esc(book.getBook().getTitle().getString(userLang)));
            }
        }
    }

    private void hasUserVars(Page page, MinervaPageInitModel m) {
        if (!m.hasUser()) {
            return;
        }
        String userLang = m.getUserLang();
        page.put("abmelden", NLS.get(userLang, "logout"));
        page.put("booksLabel", NLS.get(userLang, "books"));
        page.put("searchPlaceholder", NLS.get(userLang, "searchPlaceholder"));
        page.put("exclusionsTitle", NLS.get(userLang, "exclusions"));
        page.put("myTasks", NLS.get(userLang, "myTasks"));
        page.put("formulaEditor", NLS.get(userLang, "formulaEditor"));
        DataList list = page.list("favorites");
        if (m.getBooks() != null) {
            if (!"master".equals(m.getBranch())) {
                page.put("branch0", esc(m.getBranch()));
            }
            page.put("previewTitle", NLS.get(userLang, "preview"));
            page.put("previewlink", "/p/" + m.getBranch());
            page.put("hasBook", true);
            String linkPrefix = "/s/" + m.getBranch() + "/";
            for (String id : m.getFavorites()) {
                for (BookSO book : m.getBooks()) {
                    SeiteSO seite = book.getSeiten()._byId(id);
                    if (seite != null) {
                        DataMap map = list.add();
                        map.put("link", esc(linkPrefix + book.getBook().getFolder() + "/" + seite.getId()));
                        map.put("title", esc(seite.getTitle()));
                    }
                }
            }
        }
    }
}
