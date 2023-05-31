package minerva.book;

import minerva.model.BookSO;
import minerva.user.UPage;

public class DeleteBookPage extends UPage {

	@Override
	protected void execute() {
		String branch = ctx.pathParam("branch");
		String bookFolder = ctx.pathParam("book");

		BookSO book = user.getWorkspace(branch).getBooks().byFolder(bookFolder);

		put("branch", branch);
		put("folder", bookFolder);
		put("bookTitle", book.getBook().getTitle().getString(user.getLanguage()));
		header(n("deleteBook"));
		
		if ("d".equals(ctx.queryParam("m"))) {
			user.getWorkspace(branch).getBooks().remove(bookFolder);
			ctx.redirect("/b/" + branch);
		}
	}
}
