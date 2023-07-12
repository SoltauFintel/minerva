package minerva.book;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class BooksPage extends UPage {
    
    @Override
    public void init(Context ctx) {
        super.init(ctx);

        // set current workspace before PageInitializer is executed
        user.setCurrentWorkspace(user.getWorkspace(ctx.pathParam("branch")));
    }
    
    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String userLang = user.getGuiLanguage();
        
        WorkspaceSO workspace = user.getWorkspace(branch);
        BooksSO books = workspace.getBooks();
        String hash = "";
        String hash7 = "";
        if (MinervaWebapp.factory().isGitlab()) {
            try {
                hash = MinervaWebapp.factory().getGitlabRepository().getCommitHashOfHead(workspace);
            } catch (Exception e) {
                Logger.error("Can't load hash of HEAD commit.");
                hash = "";
            }
            if (hash != null && hash.length() > 7) {
                hash7 = hash.substring(0, 7);
            }
        }
        
        header(n("books"));
        put("branch", esc(branch));
        put("hash", esc(hash));
        put("hash7", esc(hash7));
        put("migrationAllowed", isMigrationAllowed());
        put("updateOnlineHelpAllowed", MinervaWebapp.factory().isCustomerVersion()
                && !MinervaWebapp.factory().isGitlab()
                && !StringService.isNullOrEmpty(System.getenv("SUBSCRIBERS")));
        put("addBookAllowed", !MinervaWebapp.factory().isCustomerVersion() || books.isEmpty());
        DataList list = list("books");
        if (books != null) {
            for (BookSO book : books) {
                DataMap map = list.add();
                map.put("title", esc(book.getBook().getTitle().getString(userLang)));
                map.put("folder", esc(book.getBook().getFolder()));
            }
        }
        put("booksOk", books != null);
        put("publishLink", "/rest/publish?branch=" + u(branch) + "&login=" + u(System.getenv("MP_USER")) + "&password="
                + u(System.getenv("MP_PASS")) + "&lang=" + u("de,en"));
        
        DataList list2 = list("langs");
        for (String lang : langs) {
            DataMap map = list2.add();
            map.put("lang", lang);
            map.put("selected", lang.equals(userLang));
        }
    }

    private boolean isMigrationAllowed() {
        if ("1".equals(System.getenv("MINERVA_MIGRATION"))) {
            String migrationUsers = System.getenv("MINERVA_MIGRATIONUSERS");
            if (StringService.isNullOrEmpty(migrationUsers)) {
                return true;
            }
            for (String user : migrationUsers.split(",")) {
                if (this.user.getUser().getLogin().equals(user.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
