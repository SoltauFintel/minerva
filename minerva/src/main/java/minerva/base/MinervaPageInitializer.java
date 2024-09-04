package minerva.base;

import static github.soltaufintel.amalia.web.action.Escaper.esc;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import github.soltaufintel.amalia.web.action.PageInitializer;
import minerva.MinervaWebapp;
import minerva.book.BookType;
import minerva.config.MinervaConfig;
import minerva.exclusions.SeiteSichtbar;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.SeiteSO;
import minerva.task.TaskService;
import minerva.user.CustomerMode;
import minerva.user.UserAccess;

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

        // pagemodes
        page.put("jstree", false);
        page.put("ckeditor", false);
        page.put("math", false);
        page.put("jstree", false);
        page.put("multiselect", false);
        
        page.put("hasLeftArea", false);
        page.put("leftAreaContent", "");
        page.put("guiLanguage", m.getUserLang());
        page.put("gitlab", gitlab);
        
        String qq = ctx.pathParam("branch");
        if (StringService.isNullOrEmpty(qq)) {
            page.put("isMasterBranch", true);
        } else {
            page.put("isMasterBranch", "master".equals(qq));
        }
        
        page.put("hasUser", hasUser);
        page.put("title", "Minerva");
        page.put("abmelden", "Abmelden");
        page.put("development", config.isDevelopment());
        page.put("VERSION", MinervaWebapp.VERSION);
        page.put("user", esc(m.getLogin()));
        page.put("booksLabel", "Bücher");
        page.put("Menu", "Menü");
        page.put("BranchLabel", "Branch");
        page.put("searchPlaceholder", "");
        page.put("searchFocus", false);
        page.put("delayedPush", false);
        page.put("delayedPushAllowed", false);
        page.put("endFSMode", "Turn off delayed persistence (Save)");
        page.put("previewTitle", "Preview");
        page.put("previewlink", "/p/master");
        page.put("q", "");
        page.put("showFeatureTree", false);
        page.put("featuretreeBookFolder", "");
        booksForMenu(hasUser, m.getUserLang(), m.getBooks(), page);
        page.put("isCustomerVersion", MinervaWebapp.factory().isCustomerVersion());
        page.put("branch", esc(m.getBranch()));
        page.put("exclusionsTitle", "Exclusions");
        page.put("hasBook", false);
        page.put("hasMenuItems", false);
        page.list("menuItems");
        page.list("previewBooks");
        page.put("book0Title", "");
        page.put("myTasks", "");
        page.put("isAdmin", isAdmin);
        page.put("canBeAdmin", hasUser && MinervaWebapp.factory().getAdmins().contains(m.getLogin()));
        page.put("hasExportRight", hasUser && UserAccess.hasExportRight(m.getLogin()));
        page.put("hasLastEditedPage", m.getLastEditedPage_link() != null);
        page.put("lastEditedPage_link", m.getLastEditedPage_link());
        page.put("lastEditedPage_title", m.getLastEditedPage_title());
        page.put("customerModeActive", false);
        if (m.hasUser()) {
            hasUserVars(page, m);
        }
        updateOpenMasterTasks(m, page);
    }
    
    public static void updateOpenMasterTasks(MinervaPageInitModel m, Page page) {
    	fillNumberOfOpenMasterTasks(TaskService.get(m.getUser()), page);
    }

    public static void fillNumberOfOpenMasterTasks(int omt, Page page) {
        page.put("hasOpenMasterTasks", omt > 0);
        page.putInt("numberOfOpenMasterTasks", omt);
    }

    public static void booksForMenu(boolean hasUser, String userLang, BooksSO books, Page page) {
        DataList list = page.list("booksForMenu");
        page.put("bookslinkForMenu", "/w");
        if (hasUser && books != null) {
            for (BookSO book : books) {
                if (BookType.PUBLIC.equals(book.getBook().getType()) && isVisible(book)) {
                    DataMap map = list.add();
                    map.put("folder", esc(book.getBook().getFolder()));
                    String title = book.getBook().getTitle().getString(userLang);
                    if (title.isBlank()) {
                        title = "without title";
                    }
                    map.put("title", esc(title));
				} else if (BookType.FEATURE_TREE.equals(book.getBook().getType())) {
					page.put("showFeatureTree", true);
			        page.put("featuretreeBookFolder", esc(book.getBook().getFolder()));
				}
            }
        }
    }
    
    // Customer mode
    private static boolean isVisible(BookSO book) {
        SeiteSichtbar ssc = new SeiteSichtbar(book.getWorkspace());
        for (SeiteSO seite : book.getSeiten()) {
            if (ssc.isVisible(seite)) {
                Logger.debug("Book \"" + book.getTitle() + "\" is visible because page \"" + seite.getTitle()
                        + "\" is visible [active customer mode]");
                return true;
            }
        }
        Logger.debug("Book isn't visible due to active customer mode: " + book.getTitle());
        return false; // There is no top level page that is accessible.
    }
    
    private void hasUserVars(Page page, MinervaPageInitModel m) {
        String userLang = m.getUserLang();
        page.put("abmelden", NLS.get(userLang, "logout"));
        page.put("Menu", NLS.get(userLang, "Menu"));
        page.put("BranchLabel", NLS.get(userLang, "Branch"));
        page.put("booksLabel", NLS.get(userLang, "books"));
        page.put("searchPlaceholder", NLS.get(userLang, "searchPlaceholder"));
        page.put("exclusionsTitle", NLS.get(userLang, "exclusions"));
        page.put("myTasks", NLS.get(userLang, "myTasks"));
        page.put("formulaEditor", NLS.get(userLang, "formulaEditor"));
        page.put("delayedPush", m.getUser().getUser().getDelayedPush().contains(m.getBranch()));
        page.put("delayedPushAllowed", MinervaWebapp.factory().isGitlab()
                && !"master".equals(m.getBranch())
                && !(m.getBranch().length() >= 1 && m.getBranch().charAt(0) >= '0' && m.getBranch().charAt(0) <= '9'));
        page.put("endFSMode", NLS.get(userLang, "endFSMode"));
        page.list("favorites");
        if (m.getBooks() == null) {
            return;
        }
        page.put("previewTitle", NLS.get(userLang, "preview"));
        page.put("previewlink", "/p/" + m.getBranch());
        page.put("hasBook", true);
        customerMode(m.getUser().getCustomerMode(), page);
    }
    
    public static void customerMode(CustomerMode customerMode, Page page) {
        boolean active = customerMode.isActive();
        page.put("customerModeActive", active);
        page.put("customerMode", esc(customerMode.toString()));
    }
}
