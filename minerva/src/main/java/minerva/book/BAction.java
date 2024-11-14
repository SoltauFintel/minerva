package minerva.book;

import github.soltaufintel.amalia.spark.Context;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.workspace.WAction;

/**
 * Base class for book actions
 */
public abstract class BAction extends WAction {
    protected String bookFolder;
    protected String booklink;
    protected BooksSO books;
    protected BookSO book;

    @Override
    public void init(Context ctx) {
        super.init(ctx);
        bookFolder = __book();
        booklink = "/b/" + branch + "/" + bookFolder;

        books = workspace.getBooks();
        book = books.byFolder(bookFolder);
    }
    
    protected String __book() {
    	return ctx.pathParam("book");
    }
}
