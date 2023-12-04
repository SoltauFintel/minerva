package minerva.workspace;

import minerva.model.BooksSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class WorkspacesPage extends UPage {

    @Override
    protected void execute() {
        WorkspaceSO workspace = user.getCurrentWorkspace();
        BooksSO books = workspace.getBooks();
        if (!books.isEmpty()) {
            ctx.redirect("/b/" + esc(workspace.getBranch()) + "/" + books.get(0).getBook().getFolder());
        }
    }
}
