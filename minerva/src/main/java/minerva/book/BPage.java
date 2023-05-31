package minerva.book;

import github.soltaufintel.amalia.spark.Context;
import minerva.model.BookSO;
import minerva.user.UPage;

/**
 * Base class for book pages
 */
public abstract class BPage extends UPage {
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

		put("branch", branch);
		put("bookFolder", bookFolder);
		put("booklink", booklink);
	}
}
