package minerva.export;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import minerva.MinervaWebapp;
import minerva.model.BookSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.user.User;

public class PdfTester {

	/**
	 * Start with env var MINERVA_BACKEND=file-system.
	 * C:/dat/minerva-download contains user folder which contains book folders and books.json.
	 */
	public static void main(String[] args) throws IOException {
		BookSO book = getBook("handbuch");
		System.out.println(book.getFolder());
		System.out.println(book.getSeiten().size() + ", " + book.getSeiten().get(0).getTitle());

		String lang = "de";
		PdfExportService es = new PdfExportService(book.getWorkspace(), "DLH", lang);
		boolean booksMode =! true;
		if (booksMode) {
			es.getBooksExportDownloadId(book.getWorkspace());
		} else {
			File outputFolder = es.
					saveBook(book);
//					saveSeite(book.getSeiten().get(0));
			
			System.out.println(outputFolder.getAbsolutePath());
			if (es.pdfFile != null && es.pdfFile.isFile()) {
				System.out.println(es.pdfFile.getAbsolutePath());
				Desktop.getDesktop().open(es.pdfFile);
				System.out.println("fertig");
			} else {
				throw new RuntimeException("Kein PDF");
			}
		}
	}

	private static BookSO getBook(String bookFolder) {
		MinervaWebapp.bootForTest();
		User u = new User();
		u.setLogin("Tester");
		UserSO user = new UserSO(u);
		WorkspaceSO workspace = new WorkspaceSO(user, "C:/dat", "minerva-download");
		for (BookSO book : workspace.getBooks()) {
			if (book.getBook().getFolder().equals(bookFolder)) {
				return book;
			}
		}
		throw new RuntimeException("Book not found: " + bookFolder);
	}
}
