package minerva.book;

import github.soltaufintel.amalia.spark.Context;
import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.workspace.WPage;

/**
 * Base class for book pages
 */
public abstract class BPage extends WPage {
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

        put("bookFolder", bookFolder);
        put("booklink", booklink);
    }
    
    protected String __book() {
        return ctx.pathParam("book");
    }

    protected boolean isOneLang() {
        return book.getBook().getType().isInternal();
    }
}
