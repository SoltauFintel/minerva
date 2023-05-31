package minerva.model;

import java.io.File;
import java.util.List;
import java.util.Map;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.FileService;
import minerva.base.MList;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.book.Book;
import minerva.book.Books;

public class BooksSO extends MList<BookSO> {
	private static final String DN = "books.json";
	private final WorkspaceSO workspace;
	private final Books books;
	
	public BooksSO(WorkspaceSO workspace) {
		super((a, b) -> a.getBook().getPosition() - b.getBook().getPosition());
		
		this.workspace = workspace;
		
		Books books0 = new MultiPurposeDirAccess(workspace.dao()).load(filename(), Books.class);
		if (books0 == null) {
			books = new Books();
		} else {
			books = books0;
		}
		for (Book book : books.getBooks()) {
			add(new BookSO(workspace, book));
		}
	}
	
	public void createBook(String bookFolder, NlsString title, List<String> langs, int position) {
		if (StringService.isNullOrEmpty(bookFolder)) {
			throw new UserMessage("error.enterFolder", workspace);
		}
		if (!FileService.isLegalFilename(bookFolder) || !bookFolder.equals(Escaper.esc(bookFolder))) {
			throw new RuntimeException("Folder name contains illegal characters!");
		}
		for (String lang : langs) {
			if (StringService.isNullOrEmpty(title.getString(lang))) {
				throw new UserMessage("error.enterTitle", workspace);
			}
		}
		
		for (BookSO i : this) {
			if (i.getBook().getFolder().equalsIgnoreCase(bookFolder)) {
				throw new UserMessage("error.folderAlreadyExists", workspace);
			}
		}
		
		Book book = new Book();
		book.setFolder(bookFolder);
		book.setTitle(title);
		book.setPosition(position);

		new File(workspace.getFolder() + "/" + bookFolder).mkdirs();
		
		add(new BookSO(workspace, book));
		books.getBooks().add(book);
		save("create new book " + bookFolder);
	}
	
	public void save(String commitMessage) {
		new MultiPurposeDirAccess(workspace.dao()).save(filename(), books, commitMessage, workspace);
	}
	
	public void saveTo(Map<String, String> files) {
		incVersion();
		files.put(filename(), StringService.prettyJSON(books));
	}
	
	private String filename() {
		return workspace.getFolder() + "/" + DN;
	}

	public void remove(String folder) {
		BookSO x = byFolder(folder);
		books.getBooks().removeIf(i -> i.getFolder().equals(folder));
		remove(x);
		save("delete book " + folder);
	}
	
	public BookSO byFolder(String folder) {
		for (BookSO book : this) {
			if (book.getBook().getFolder().equals(folder)) {
				return book;
			}
		}
		throw new RuntimeException("Book does not exist!");
	}
	
	public int calculateNextPosition() {
		int max = 0;
		for (BookSO book : this) {
			int p = book.getBook().getPosition();
			if (p > max) {
				max = p;
			}
		}
		return max + 1;
	}

	public void incVersion() {
		books.setVersion(books.getVersion() + 1);
	}
}
