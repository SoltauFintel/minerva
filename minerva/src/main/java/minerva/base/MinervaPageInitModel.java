package minerva.base;

import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.spark.Context;
import minerva.model.BooksSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;

public class MinervaPageInitModel {
    private final String login;
    private final boolean hasUser;
    private String branch;
    private String userLang;
    private BooksSO books;
    private List<String> favorites;
    
    public MinervaPageInitModel(Context ctx) {
        WebContext wctx = new WebContext(ctx);
        login = wctx.session().getLogin();
        hasUser = wctx.session().isLoggedIn();
        branch = "";
        userLang = "de";
        if (hasUser) {
            UserSO user = StatesSO.get(ctx).getUser();
            userLang = user.getGuiLanguage();
            favorites = user.getFavorites();
            if (user.getCurrentWorkspace() != null) {
                branch = user.getCurrentWorkspace().getBranch();
                try {
                    if (user.getCurrentWorkspace().getBooks() != null //
                            && !user.getCurrentWorkspace().getBooks().isEmpty()) {
                        books = user.getCurrentWorkspace().getBooks();
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

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }
}
