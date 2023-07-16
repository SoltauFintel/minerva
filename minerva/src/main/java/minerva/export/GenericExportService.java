package minerva.export;

import java.io.File;

import minerva.MinervaWebapp;
import minerva.base.FileService;
import minerva.base.NLS;
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
    /** language, e.g. "en" */
    protected final String lang;
    protected ExclusionsService exclusionsService;
    protected BookSO currentBook = null;

    public GenericExportService(WorkspaceSO workspace, String customer, String language) {
        lang = language;
        exclusionsService = new ExclusionsService();
        exclusionsService.setCustomer(customer);
        exclusionsService.setExclusions(new Exclusions(workspace.getExclusions().get()));
    }
    
    protected String getCustomer() {
        return exclusionsService.getCustomer().toUpperCase();
    }

    public File saveWorkspace(WorkspaceSO workspace) {
        File outputFolder = getFolder(NLS.get(lang, "allBooks"));
        init(outputFolder);
        for (BookSO book : workspace.getBooks()) {
            saveBookTo(book, new File(outputFolder, FileService.getSafeName(book.getBook().getFolder())));
        }
        return outputFolder;
    }

    public File saveBook(BookSO book) {
        File outputFolder = getFolder(book.getBook().getFolder());
        saveBookTo(book, outputFolder);
        return outputFolder;
    }
    
    protected void saveBookTo(BookSO book, File outputFolder) {
        currentBook = book;
        init(outputFolder);
        saveSeitenTo(book.getSeiten(lang), null, outputFolder);
    }
    
    public void saveSeitenTo(SeitenSO seiten, SeiteSO parent, File outputFolder) {
        for (SeiteSO seite : seiten) {
            _saveSeiteTo(seite, parent, outputFolder);
        }
    }
    
    public File saveSeite(SeiteSO seite) {
        File outputFolder = getFolder(seite.getSeite().getTitle().getString(lang));
        init(outputFolder);
        if (!_saveSeiteTo(seite, null, outputFolder)) {
            throw new RuntimeException("Page #" + seite.getId() + " \"" + seite.getTitle() + "\" is not visible!");
        }
        return outputFolder;
    }
    
    private boolean _saveSeiteTo(SeiteSO seite, SeiteSO parent, File outputFolder) {
        if (seite.isVisible(exclusionsService, lang).isVisible()) {
            saveSeiteTo(seite, parent, outputFolder);
            saveSeitenTo(seite.getSeiten(), seite, outputFolder);
            return true;
        }
        return false;
    }
    
    protected abstract void saveSeiteTo(SeiteSO seite, SeiteSO parent, File outputFolder);
    
    private File getFolder(String name) {
        File folder = new File(MinervaWebapp.factory().getWorkFolder("export"), FileService.getSafeName(name));
        FileService.deleteFolder(folder);
        return folder;
    }

    protected abstract void init(File outputFolder);
}
