package minerva.book;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
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
        String userLang = user.getLanguage();
        
        WorkspaceSO workspace = user.getWorkspace(branch);
        BooksSO books = workspace.getBooks();
        String hash = "";
        String hash7 = "";
        if (MinervaWebapp.factory().isGitlab()) {
            hash = MinervaWebapp.factory().getGitlabRepository().getCommitHashOfHead(workspace);
            if (hash != null && hash.length() > 7) {
                hash7 = hash.substring(0, 7);
            }
        }
        
        header(n("books"));
        put("branch", esc(branch));
        put("hash", esc(hash));
        put("hash7", esc(hash7));
        put("migrationAllowed", "1".equals(System.getenv("MINERVA_MIGRATION")));
        DataList list = list("books");
        for (BookSO book : books) {
            DataMap map = list.add();
            map.put("title", esc(book.getBook().getTitle().getString(userLang)));
            map.put("folder", esc(book.getBook().getFolder()));
        }
        
        DataList list2 = list("langs");
        for (String lang : langs) {
            DataMap map = list2.add();
            map.put("lang", lang);
            map.put("selected", lang.equals(userLang));
        }
    }
}
