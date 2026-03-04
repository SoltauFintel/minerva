package minerva.workspace;

import org.pmw.tinylog.Logger;

import minerva.model.BooksSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class WorkspacesPage extends UPage {

    @Override
    protected void execute() {
        WorkspaceSO workspace = user.getCurrentWorkspace();
        BooksSO books = workspace.getBooks();
        if (books.isEmpty()) {
            Logger.info(user.getLogin() + " | WorkspacesPage: no books");
        } else {
            String url = "/b/" + esc(workspace.getBranch()) + "/" + books.get(0).getBook().getFolder();
            Logger.info(user.getLogin() + " | WorkspacesPage: " + url);
            
            ctx.redirect(url);
        }
    }
}
