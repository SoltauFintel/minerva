package minerva.book;

import github.soltaufintel.amalia.spark.Context;
import minerva.model.BookSO;
import minerva.user.UAction;

/**
 * Base class for book actions
 */
public abstract class BAction extends UAction {
    protected String branch;
    protected String bookFolder;
    protected String booklink;
    protected BookSO book;

    @Override
    public void init(Context ctx) {
        super.init(ctx);
        branch = ctx.pathParam("branch");
        bookFolder = ctx.pathParam("book");
        booklink = "/b/" + branch + "/" + bookFolder;

        book = user.getWorkspace(branch).getBooks().byFolder(bookFolder);
    }
}
