package minerva.book;

import java.util.ArrayList;
import java.util.List;

public class Books {
	private final List<Book> books = new ArrayList<>();
	private int version = 1;
	// TO-DO languages (Die Sprachen werden pro Branch eingestellt und in der books.json gespeichert.)
	
	public List<Book> getBooks() {
		return books;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Deprecated
	public Book find(String folder) {
		return books.stream().filter(book -> book.getFolder().equals(folder)).findFirst().orElse(null);
	}
	
	public Book byFolder(String folder) {
		return books.stream().filter(book -> book.getFolder().equals(folder)).findFirst().orElse(null);
	}
}
