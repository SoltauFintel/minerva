package minerva.book;

import com.github.template72.data.DataCondition;
import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import gitper.base.StringService;
import minerva.MinervaWebapp;
import minerva.base.MinervaMetrics;
import minerva.config.MinervaFactory;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.workspace.WPage;

public class MenuPage extends WPage {
    private int counter = 0;

    @Override
    protected void execute() {
        header(n("Menu"));
        workspaces();
        menu();
        favorites();
        put("persistenceInfo", esc(MinervaWebapp.factory().getBackendService().getInfo(user.getGuiLanguage())));
        put("coloredHeadings", n(user.isColoredHeadings() ? "turnOffColoredHeadings" : "turnOnColoredHeadings"));
        MinervaMetrics.MENU.inc();
    }

    private void workspaces() {
        DataList list = list("workspaces");
        if (MinervaWebapp.factory().isGitlab()) {
            for (WorkspaceSO workspace : user.getWorkspaces()) {
                DataMap map = list.add();
                
                map.put("link", "/w/" + esc(workspace.getBranch()) + "/menu");
                map.put("text", esc(workspace.getBranch()));
                if (branch.equals(workspace.getBranch())) {
                    map.put("icon", "fa-folder-open-o currentBranchIcon");
                    map.put("isCurrent", true);
                } else {
                    map.put("icon", "fa-folder-o");
                    map.put("isCurrent", false);
                }
            }
        }
    }

    private void menu() {
        MinervaFactory fac = MinervaWebapp.factory();
        boolean isAdmin = UserSO.isAdmin(ctx);
        boolean booksOk = workspace.getBooks() != null && !workspace.getBooks().isEmpty();
        DataList list = list("commands");
        menu(list, "myTasks", "fa-inbox", "/w/:branch/my-tasks");
        if (!fac.isCustomerVersion()) {
            menu(list, "customerMode", "fa-thumbs-o-up", "/w/:branch/customer-mode/na");
        }
        if (fac.isGitlab()) {
            menu(list, "workspaceHistory", "fa-history", "/w/:branch/history");
        }
        if (booksOk) {
            menu(list, "tagCloud", "fa-cloud", "/w/:branch/tag-cloud");
        }
        for (BookSO bookSO : workspace.getBooks()) {
            if (bookSO.isInternal()) {
                menu(list, bookSO.getBook().getTitle().getString(user.getGuiLanguage()), "fa-book fa-internal", "/b/:branch/" + bookSO.getBook().getFolder());
            } else if (bookSO.isFeatureTree()) {
                menu(list, bookSO.getBook().getTitle().getString(user.getGuiLanguage()), "fa-sitemap fa-sitemap-color", "/b/:branch/" + bookSO.getBook().getFolder());
            }
        }
        if (!fac.isCustomerVersion()) {
            menu(list, "allHelpKeys", "fa-question-circle", "/w/:branch/help-keys");
        }
        menu(list, "formulaEditor", "fa-superscript", "/math");
        onlinehelp(fac, booksOk, list);
        if (fac.isCustomerVersion()) {
            menu(list, "Broken Mappings", "fa-chain-broken", "/w/:branch/broken-mappings");
        } else {
            menu(list, "Broken Links", "fa-chain-broken", "/w/:branch/broken-links");
        }
        if (fac.isCustomerVersion()) {
            menu(list, "papierkorb", "fa-recycle", "/w/:branch/recycle");
        }
        export(booksOk, list);
        workspace(list);
        if (!fac.isCustomerVersion()) {
            menu(list, "keyValues", "fa-key", "/values/:branch");
        }
        additionalMenuItems(list);
        menu(list, user.getUser().isShowQuickbuttons() ? "hideQuickButtons" : "showQuickButtons", "fa-space-shuttle",
                "/w/:branch/toggle-quickbuttons");
        admin(fac, isAdmin, booksOk, list);
        if (booksOk && fac.isCustomerVersion() && !fac.getAdmins().contains(user.getLogin())) {
            menu(list, "reindex", "fa-refresh", "/w/:branch/index", true);
        }
    }

    private void workspace(DataList list) {
        if (MinervaWebapp.factory().isGitlab()) {
            menu(list, "pullWS", "fa-refresh", "/w/:branch/pull", true);
            menu(list, "cloneWS", "fa-refresh red", "/w/:branch/pull?force=1", true);
            menu(list, "createWS", "fa-folder", "/create-workspace");
            menu(list, "deleteWS", "fa-trash-o red", "/w/:branch/delete");
            menu(list, "createBranch", "fa-code-fork", "/branch/:branch");
            menu(list, "mergeBranch", "fa-code-fork", "/merge/:branch");
            if (isDelayedPushAllowed()) {
                if (user.getUser().getDelayedPush().contains(branch)) {
                    menu(list, "endFSMode", "fa-flag-checkered fsmode", "/w/:branch/deactivate-f-s-mode");
                } else {
                    menu(list, "beginFSMode", "fa-flag-checkered", "/w/:branch/activate-f-s-mode");
                }
            }
        }
    }
    
    private boolean isDelayedPushAllowed() {
        return MinervaWebapp.factory().isGitlab()
                && !"master".equals(branch)
                && !(branch.length() >= 1 && branch.charAt(0) >= '0' && branch.charAt(0) <= '9');
    }

    protected void additionalMenuItems(DataList list) { // template method
    }

    protected void additionalAdminMenuItems(DataList list) { // template method
    }

    private void onlinehelp(MinervaFactory fac, boolean booksOk, DataList list) {
        if (booksOk
                && fac.isCustomerVersion()
                && !fac.isGitlab()
                && fac.getConfig().hasSubscribers()) {
            menu(list, "updateOnlineHelp", "fa-upload darkgreen", "/w/:branch/push-data");
        }
    }

    private void export(boolean booksOk, DataList list) {
        if (booksOk) {
            menu(list, "export", "fa-upload", "/w/:branch/export");
        }
    }

    private void admin(MinervaFactory fac, boolean isAdmin, boolean booksOk, DataList list) {
        if (!MinervaWebapp.factory().getAdmins().contains(user.getLogin())) {
            return;
        }
        if (isAdmin) {
            menu(list, "dropAdminRights", "fa-trophy", "/activate-admin-rights?m=0");
            menu(list, "Configuration", "fa-cogs", "/config");
            menu(list, "books", "fa-book", "/w/:branch");
            if (fac.isGitlab()) {
                menu(list, "exclusions", "fa-bank", "/w/:branch/exclusions/edit");
            }
            menu(list, "manageUsers", "fa-users", "/users");
            if (booksOk) {
                menu(list, "reindex", "fa-refresh", "/w/:branch/index", true);
            }
            menu(list, "serverlog", "fa-paw", "/serverlog");
            if (isMigrationAllowed()) {
                menu(list, "Confluence Import", "fa-cloud-download", "/migration/:branch");
            }
            additionalAdminMenuItems(list);
        } else {
            menu(list, "giveAdminRights", "fa-trophy", "/activate-admin-rights");
        }
    }
    
    protected void menu(DataList list, String text, String icon, String link) {
        menu(list, text, icon, link, false);
    }

    protected void menu(DataList list, String text, String icon, String link, boolean waitDisplay) {
        DataMap map = list.add();
        map.put("id", "i" + counter++);
        map.put("text", esc(n(text)));
        map.put("icon", esc(icon));
        map.put("link", esc(link.replace(":branch", esc(branch))));
        map.put("sep", "-".equals(text));
        map.put("spin", waitDisplay);
    }

    private boolean isMigrationAllowed() {
        if ("1".equals(MinervaWebapp.factory().getConfig().getMigration())) {
            String migrationUsers = MinervaWebapp.factory().getConfig().getMigrationUsers();
            if (migrationUsers.isEmpty()) {
                return true;
            }
            for (String user : migrationUsers.split(",")) {
                if (this.user.getLogin().equals(user.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void favorites() {
        boolean hasFavorites = ((DataCondition) model.get("hasLastEditedPage")).isTrue();
        DataList list = list("favorites");
        String linkPrefix = "/s/" + branch + "/";
        WorkspaceSO _workspace = user.getWorkspace(branch);
        for (String id : user.getFavorites()) {
            SeiteSO seite = _workspace.findPage(id);
            if (seite != null) {
                DataMap map = list.add();
                map.put("link", esc(linkPrefix + seite.getBook().getBook().getFolder() + "/" + seite.getId()));
                map.put("title", esc(seite.getTitle()));
                hasFavorites = true;
            }
        }
        StringService.sortDataListUmlaute(list, "title");
        put("hasFavorites", hasFavorites);
    }
}
