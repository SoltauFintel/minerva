package minerva.export;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.timer.AbstractTimer;
import gitper.base.FileService;
import minerva.MinervaWebapp;
import minerva.base.NLS;
import minerva.exclusions.SeiteSichtbar;
import minerva.exclusions.Visible;
import minerva.export.SomeSubpages.SeiteAndDone;
import minerva.export.pdf.Bookmark;
import minerva.export.pdf.Chapter;
import minerva.export.pdf.PdfExportService;
import minerva.export.template.ExportTemplateSet;
import minerva.export.template.ExportTemplatesService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;
import minerva.user.CustomerMode;

/**
 * Output format unspecific export service
 * can export books, a book, pages and a page
 * for a customer and one language.
 */
public abstract class GenericExportService {
    private static final Map<String, File> downloads = new HashMap<>();
    protected final ExportRequest req;
    protected final String lang;
    protected final ExportTemplateSet exportTemplateSet;
    protected final SeiteSichtbar ssc;
    protected BookSO currentBook = null;
    protected boolean booksMode = false;
    /** current parent bookmark */
    protected Bookmark cb = Bookmark.root();
    protected List<Bookmark> bookmarks = cb.getBookmarks();

    public GenericExportService(ExportRequest req) {
        this.req = req;
        lang = req.getLanguage();
        exportTemplateSet = new ExportTemplatesService(req.getWorkspace()).load(req.getTemplateId());
        ssc = new SeiteSichtbar(req.getWorkspace(), new CustomerMode(req.getCustomer()), req.pdf(), req.getLanguage());
    }
    
    protected String getCustomer() {
        return req.getCustomer().toUpperCase();
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
            if (book.getBook().getType().isPublic() && book.hasContent(ssc)) {
                String bookFolder = FileService.getSafeName(book.getBook().getFolder());
                saveBookTo(book, new File(outputFolder, bookFolder));
            } else {
            	Logger.info("omitting non-public book \"" + book.getTitle() + "\"");
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
        saveSeitenTo(book.getSeiten(lang), null, new Chapter(), /*all subpages*/ i -> i.getSeiten(), outputFolder);
    }
    
    public void saveSeitenTo(Iterable<SeiteSO> seiten, SeiteSO parent, Chapter chapter, SubpagesSelector ss, File outputFolder) {
        chapter = chapter.child();
        for (SeiteSO seite : seiten) {
            if (_saveSeiteTo(seite, parent, chapter, ss, outputFolder)) {
                chapter = chapter.inc();
            }
        }
    }

    public String getSeitenExportDownloadId(List<SeiteSO> seiten) {
        return prepareDownload(saveSeiten(seiten));
    }

    public File saveSeiten(List<SeiteSO> seiten) {
        File outputFolder = getFolder(seiten.get(0).getSeite().getTitle().getString(lang));
        init(outputFolder);
        SomeSubpages subpagesSelector = new SomeSubpages(seiten);
        if (req.withChapters()) {
            Chapter chapter = new Chapter().child();
            for (SeiteAndDone sd : subpagesSelector.getAllPages()) {
                if (!sd.isDone()) {
                    _saveSeiteTo(sd.getSeite(), null, chapter, subpagesSelector, outputFolder);
                    chapter = chapter.inc();
                }
            }
        } else {
            for (SeiteAndDone sd : subpagesSelector.getAllPages()) {
                if (!sd.isDone()) {
                    _saveSeiteTo(sd.getSeite(), null, Chapter.withoutChapters(), subpagesSelector, outputFolder);
                }
            }
        }
        return outputFolder;
    }
    
    private boolean _saveSeiteTo(SeiteSO seite, SeiteSO parent, Chapter chapter, SubpagesSelector ss, File outputFolder) {
        Visible visible = ssc.getVisibleResult(seite);
    	if (visible.isVisible()) {
            if (saveSeiteTo(seite, parent, chapter, outputFolder)) {

                Bookmark keep = cb; // remember
                keep.getBookmarks().add(cb = new Bookmark(seite, lang, chapter, req.withChapters(), visible.isNoTree()));
                
                saveSeitenTo(ss.getSubpages(seite), seite, chapter, ss, outputFolder);
                
                cb = keep; // restore
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param seite -
     * @param parent -
     * @param chapter -
     * @param outputFolder -
     * @return false: no page has been added
     */
    protected abstract boolean saveSeiteTo(SeiteSO seite, SeiteSO parent, Chapter chapter, File outputFolder);
    
    private File getFolder(String name) {
        File folder = new File(
                MinervaWebapp.factory().getWorkFolder("export_" + IdGenerator.createId6().toUpperCase()),
                FileService.getSafeName(name));
        Logger.info("export work folder: " + folder.getAbsolutePath());
        FileService.deleteFolder(folder); // ensure empty folder
        return folder;
    }

    protected abstract void init(File outputFolder);

    public static GenericExportService getService(ExportRequest req) {
        if (req.pdf()) {
            return new PdfExportService(req);
        } else { // Multi page HTML
            return new MultiPageHtmlExportService(req);
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
        File r = new File(file.getAbsolutePath()); // recreate File so that .getParentFile().getParentFile() will work later
        downloads.put(id, r);
        return id;
    }
    
    public static String getFilename(String id) {
        File file = downloads.get(id);
        return file == null ? null : file.getName();
    }
    
    public static File get(String id) {
        return downloads.get(id);
    }
    
    public static void cleanup() {
		if (downloads.size() > 0) {
			downloads.clear();
		}
		File[] files = new File(MinervaWebapp.factory().getConfig().getWorkFolder()).listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory() && file.getName().startsWith("export_")) {
					FileService.deleteFolder(file);
				}
			}
		}
    }
    
    public static class CleanupExportFolderTimer extends AbstractTimer {

		@Override
		protected void timerEvent() {
			GenericExportService.cleanup();
		}
    }
}
