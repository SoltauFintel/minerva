package minerva.base;

import static github.soltaufintel.amalia.web.action.Escaper.esc;
import static minerva.base.StringService.umlaute;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import github.soltaufintel.amalia.web.action.PageInitializer;
import minerva.MinervaWebapp;
import minerva.config.MinervaConfig;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.SeiteSO;

public class MinervaPageInitializer extends PageInitializer {
    
    @Override
    public void initPage(Context ctx, Page page) {
        MinervaPageInitModel m = new MinervaPageInitModel(ctx);
        MinervaConfig config = MinervaWebapp.factory().getConfig();
        boolean gitlab = config.isGitlab();
        boolean hasUser = m.hasUser();
        boolean isAdmin = "1".equals(ctx.req.session().attribute("admin"));
        if (page instanceof Uptodatecheck
                && gitlab
                && m.getUser() != null && m.getBranch() != null
                && m.getUser().popHasToPull(m.getBranch())) {
            m.getUser().getWorkspace(m.getBranch()).pull();
            page.init(ctx);
        }
        page.put("gitlab", gitlab);
        page.put("hasUser", hasUser);
        page.put("title", "Minerva");
        page.put("abmelden", "Abmelden");
        page.put("development", config.isDevelopment());
        page.put("VERSION", MinervaWebapp.VERSION);
        page.put("user", esc(m.getLogin()));
        page.put("booksLabel", "Bücher");
        page.put("searchPlaceholder", "");
        page.put("searchFocus", false);
        page.put("delayedPush", false);
        page.put("delayedPushAllowed", false);
        page.put("branch0", "");
        page.put("previewTitle", "Preview");
        page.put("previewlink", "/p/master");
        page.put("q", "");
        booksForMenu(hasUser, m.getUserLang(), m.getBooks(), page);
        page.put("isCustomerVersion", MinervaWebapp.factory().isCustomerVersion());
        page.put("branch", esc(m.getBranch()));
        page.put("exclusionsTitle", "Exclusions");
        page.put("hasBook", false);
        page.put("book0Title", "");
        page.put("myTasks", "");
        page.put("isAdmin", isAdmin);
        page.put("canBeAdmin", hasUser && MinervaWebapp.factory().getAdmins().contains(m.getLogin()));
        page.put("hasExportRight", hasUser && MinervaWebapp.factory().getPersonsWithExportRight().contains(m.getLogin()));
        page.put("hasLastEditedPage", m.getLastEditedPage_link() != null);
        page.put("lastEditedPage_link", m.getLastEditedPage_link());
        page.put("lastEditedPage_title", m.getLastEditedPage_title());
        if (m.hasUser()) {
            hasUserVars(page, m);
        }
    }

    private void booksForMenu(boolean hasUser, String userLang, BooksSO books, Page page) {
        DataList list = page.list("booksForMenu");
        page.put("bookslinkForMenu", "/w");
        if (hasUser && books != null) {
            for (BookSO book : books) {
                DataMap map = list.add();
                map.put("folder", esc(book.getBook().getFolder()));
                map.put("title", esc(book.getBook().getTitle().getString(userLang)));
            }
        }
    }

    private void hasUserVars(Page page, MinervaPageInitModel m) {
        String userLang = m.getUserLang();
        page.put("abmelden", NLS.get(userLang, "logout"));
        page.put("booksLabel", NLS.get(userLang, "books"));
        page.put("searchPlaceholder", NLS.get(userLang, "searchPlaceholder"));
        page.put("exclusionsTitle", NLS.get(userLang, "exclusions"));
        page.put("myTasks", NLS.get(userLang, "myTasks"));
        page.put("formulaEditor", NLS.get(userLang, "formulaEditor"));
        page.put("delayedPush", m.getUser().getUser().getDelayedPush().contains(m.getBranch()));
        page.put("delayedPushAllowed", MinervaWebapp.factory().isGitlab()
                && !"master".equals(m.getBranch())
                && !(m.getBranch().length() >= 1 && m.getBranch().charAt(0) >= '0' && m.getBranch().charAt(0) <= '9'));
        DataList list = page.list("favorites");
        if (m.getBooks() == null) {
            return;
        }
        if (!"master".equals(m.getBranch())) {
            page.put("branch0", esc(m.getBranch()));
        }
        page.put("previewTitle", NLS.get(userLang, "preview"));
        page.put("previewlink", "/p/" + m.getBranch());
        page.put("hasBook", true);
        String linkPrefix = "/s/" + m.getBranch() + "/";
        for (String id : m.getFavorites()) {
            for (BookSO book : m.getBooks()) {
                SeiteSO seite = book._seiteById(id);
                if (seite != null) {
                    DataMap map = list.add();
                    map.put("link", esc(linkPrefix + book.getBook().getFolder() + "/" + seite.getId()));
                    map.put("title", esc(seite.getTitle()));
                }
            }
        }
        list.sort((a, b) -> umlaute(a.get("title").toString()).compareTo(umlaute(b.get("title").toString())));
    }
}
