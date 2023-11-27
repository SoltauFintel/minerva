package minerva.export;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
import minerva.base.FileService;
import minerva.base.NLS;
import minerva.base.UserMessage;
import minerva.exclusions.Exclusions;
import minerva.exclusions.ExclusionsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;

/**
 * Output format unspecific export service
 * can export books, a book, pages and a page
 * for a customer and one language.
 */
public abstract class GenericExportService {
	private static final Map<String, File> downloads = new HashMap<>();
    /** language, e.g. "en" */
    protected final String lang;
    protected ExclusionsService exclusionsService;
    protected BookSO currentBook = null;
    protected boolean booksMode = false;
    /** current parent bookmark */
    protected Bookmark cb = new Bookmark("root", "book");
    protected List<Bookmark> bookmarks = cb.getBookmarks();

    public GenericExportService(WorkspaceSO workspace, String customer, String language) {
        lang = language;
        exclusionsService = new ExclusionsService();
        exclusionsService.setCustomer(customer);
        exclusionsService.setExclusions(new Exclusions(workspace.getExclusions().get()));
    }
    
    protected String getCustomer() {
        return exclusionsService.getCustomer().toUpperCase();
    }

    public String getBooksExportDownloadId(WorkspaceSO workspace) {
    	return prepareDownload(saveWorkspace(workspace));
    }
    
    /**
     * @param workspace with at least 1 book
     * @return output folder
     */
    public File saveWorkspace(WorkspaceSO workspace) {
        booksMode = true;
        File outputFolder = getFolder(NLS.get(lang, "allBooks"));
        Logger.info("export books output folder: " + outputFolder.getAbsolutePath());
        for (BookSO book : workspace.getBooks()) {
            if (book.hasContent(lang, exclusionsService)) {
                String bookFolder = FileService.getSafeName(book.getBook().getFolder());
                saveBookTo(book, new File(outputFolder, bookFolder));
            }
        }
        return outputFolder;
    }

    public String getBookExportDownloadId(BookSO book) {
    	return prepareDownload(saveBook(book));
    }

    public File saveBook(BookSO book) {
        sort(book.getSeiten()); // sort once at begin, because later it's not possible
        File outputFolder = getFolder(book.getBook().getFolder());
        saveBookTo(book, outputFolder);
        return outputFolder;
    }
    
    private void sort(SeitenSO seiten) {
        seiten.sort(lang);
        for (SeiteSO seite : seiten) {
            sort(seite.getSeiten()); // recursive
        }
    }
    
    protected void saveBookTo(BookSO book, File outputFolder) {
        currentBook = book;
        outputFolder = new File(outputFolder, "html");
        init(outputFolder);
        saveSeitenTo(book.getSeiten(lang), null, new Chapter(), outputFolder);
    }
    
    public void saveSeitenTo(SeitenSO seiten, SeiteSO parent, Chapter chapter, File outputFolder) {
    	chapter = chapter.child();
        for (SeiteSO seite : seiten) {
            if (_saveSeiteTo(seite, parent, chapter, outputFolder)) {
            	chapter = chapter.inc();
            }
        }
    }

    public String getSeiteExportDownloadId(SeiteSO seite) {
    	return prepareDownload(saveSeite(seite));
    }

    public File saveSeite(SeiteSO seite) {
        String title = seite.getSeite().getTitle().getString(lang);
		File outputFolder = getFolder(title);
        init(outputFolder);
        if (!_saveSeiteTo(seite, null, Chapter.withoutChapters(), outputFolder)) {
            throw new UserMessage("export-page-is-not-visible", seite.getBook().getWorkspace(), msg -> msg.replace("$t", title));
        }
        return outputFolder;
    }
    
    private boolean _saveSeiteTo(SeiteSO seite, SeiteSO parent, Chapter chapter, File outputFolder) {
        if (seite.isVisible(exclusionsService, lang).isVisible()) {
        	saveSeiteTo(seite, parent, chapter, outputFolder);

            Bookmark keep = cb; // remember
    		keep.getBookmarks().add(cb = new Bookmark(seite, lang, chapter));
        	
            saveSeitenTo(seite.getSeiten(), seite, chapter, outputFolder);
            
            cb = keep; // restore
            return true;
        }
        return false;
    }
    
    protected abstract void saveSeiteTo(SeiteSO seite, SeiteSO parent, Chapter chapter, File outputFolder);
    
    private File getFolder(String name) {
        File folder = new File(MinervaWebapp.factory().getWorkFolder("export"), FileService.getSafeName(name));
        FileService.deleteFolder(folder);
        return folder;
    }

    protected abstract void init(File outputFolder);

    public static GenericExportService getService(WorkspaceSO workspace, String customer, String language, Context ctx) {
        String w = ctx.queryParam("w");
        if ("pdf".equals(w)) {
            return new PdfExportService(workspace, customer, language);
        } else { // Multi page HTML
            return new MultiPageHtmlExportService(workspace, customer, language);
        }
    }
    
	private String prepareDownload(File sourceFolder) {
		String id;
		File pdfFile = new File(sourceFolder, sourceFolder.getName() + ".pdf");
		if (pdfFile.isFile()) {
			id = register(pdfFile);
			Logger.info(pdfFile.getAbsolutePath() + " => " + id);
		} else {
			File zipFile = new File(sourceFolder.getParentFile(), sourceFolder.getName() + ".zip");
			FileService.zip(sourceFolder, zipFile);
			id = register(zipFile);
			Logger.info(zipFile.getAbsolutePath() + " => " + id);
		}
		return id;
	}

	public static String register(File file) {
		String id = IdGenerator.createId6();
		downloads.put(id, file);
		return id;
	}
	
	public static String getFilename(String id) {
		File file = downloads.get(id);
		return file == null ? null : file.getName();
	}
	
	public static File pop(String id) {
		File ret = downloads.get(id);
		downloads.remove(id);
		return ret;
	}
}
