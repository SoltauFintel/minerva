package minerva.base;

import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.spark.Context;
import minerva.model.BooksSO;
import minerva.model.SeiteSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;

public class MinervaPageInitModel {
    private final String login;
    private final boolean hasUser;
    private String branch;
    private UserSO user;
    private String userLang;
    private BooksSO books;
    private List<String> favorites;
    private String lastEditedPage_link;
    private String lastEditedPage_title;
    
    public MinervaPageInitModel(Context ctx) {
        WebContext wctx = new WebContext(ctx);
        login = wctx.session().getLogin();
        hasUser = wctx.session().isLoggedIn();
        branch = "";
        userLang = "de";
        lastEditedPage_link = null;
        lastEditedPage_title = null;
        if (hasUser) {
            user = StatesSO.get(ctx).getUser();
            userLang = user.getGuiLanguage();
            favorites = user.getFavorites();
            WorkspaceSO workspace = user.getCurrentWorkspace();
            if (workspace != null) {
                branch = workspace.getBranch();
                try {
                    if (workspace.getBooks() != null && !workspace.getBooks().isEmpty()) {
                        books = workspace.getBooks();
                        String id = user.getUser().getLastEditedPage();
                        if (!StringService.isNullOrEmpty(id)) {
                            SeiteSO les = workspace.findPage(id);
                            if (les != null) {
                                lastEditedPage_title = les.getTitle();
                                lastEditedPage_link = "/s/" + branch + "/" + les.getBook().getBook().getFolder() + "/" + id;
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.error(e, "Error while accessing books in MinervaPageInitModel");
                    books = null;
                }
            }
        }
    }

    public String getLogin() {
        return login;
    }

    public boolean hasUser() {
        return hasUser;
    }

    public UserSO getUser() {
        return user;
    }

    public String getUserLang() {
        return userLang;
    }

    public BooksSO getBooks() {
        return books;
    }

    public String getBranch() {
        return branch;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public String getLastEditedPage_link() {
        return lastEditedPage_link;
    }

    public String getLastEditedPage_title() {
        return lastEditedPage_title;
    }
}
