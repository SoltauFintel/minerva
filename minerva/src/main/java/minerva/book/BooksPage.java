package minerva.book;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class BooksPage extends UPage {
	
	@Override
	protected void execute() {
		String branch = ctx.pathParam("branch");
		String userLang = user.getLanguage();
		
		WorkspaceSO workspace = user.getWorkspace(branch);
		user.setCurrentWorkspace(workspace);
		BooksSO books = workspace.getBooks();
		
		header(n("books"));
		put("branch", branch);
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
