package minerva.book;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
import minerva.access.CommitHash;
import minerva.base.DeliverHtmlContent;
import minerva.config.MinervaFactory;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class BooksPage extends UPage {
    public static DeliverHtmlContent<WorkspaceSO> additionalButtons = i -> "";
    
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
        
        user.onlyAdmin();
        WorkspaceSO workspace = user.getWorkspace(branch);
        BooksSO books = workspace.getBooks();
        CommitHash hash = workspace.getCommitHash();
        MinervaFactory fac = MinervaWebapp.factory();
        
        header(n("books"));
        put("branch", esc(branch));
        put("hash", esc(hash.getHash()));
        put("hash7", esc(hash.getShortHash()));
        put("migrationAllowed", isMigrationAllowed());
        put("updateOnlineHelpAllowed",
                    fac.isCustomerVersion()
                && !fac.isGitlab()
                && !fac.getConfig().getSubscribers().isEmpty());
        put("addBookAllowed", !fac.isCustomerVersion() || books.isEmpty());
        DataList list = list("books");
        if (books != null) {
            for (BookSO book : books) {
                DataMap map = list.add();
                map.put("title", esc(book.getBook().getTitle().getString(userLang)));
                map.put("folder", esc(book.getBook().getFolder()));
            }
        }
        put("booksOk", books != null);
        put("workspaceNotOk", n("workspaceNotOk").replace("$b", esc(branch)));
        put("userMessage", esc(workspace.getUserMessage()));
        put("additionalButtons", additionalButtons.getHTML(workspace));
        
        DataList list2 = list("langs");
        for (String lang : langs) {
            DataMap map = list2.add();
            map.put("lang", lang);
            map.put("selected", lang.equals(userLang));
        }
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
}
