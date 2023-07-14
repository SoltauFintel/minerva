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
        File ret = getFolder(NLS.get(lang, "allBooks"));
        for (BookSO book : workspace.getBooks()) {
            saveBookTo(book, new File(ret, FileService.getSafeName(book.getBook().getFolder())));
        }
        return ret;
    }

    public File saveBook(BookSO book) {
        File outputFolder = getFolder(book.getBook().getFolder());
        saveBookTo(book, outputFolder);
        return outputFolder;
    }
    
    protected void saveBookTo(BookSO book, File outputFolder) {
        saveSeitenTo(book.getSeiten(lang), outputFolder);
    }
    
    public void saveSeitenTo(SeitenSO seiten, File outputFolder) {
        for (SeiteSO seite : seiten) {
            _saveSeiteTo(seite, outputFolder);
        }
    }
    
    public File saveSeite(SeiteSO seite) {
        File outputFolder = getFolder(seite.getSeite().getTitle().getString(lang));
        if (!_saveSeiteTo(seite, outputFolder)) {
            throw new RuntimeException("Page #" + seite.getId() + " \"" + seite.getTitle() + "\" is not visible!");
        }
        return outputFolder;
    }
    
    private boolean _saveSeiteTo(SeiteSO seite, File outputFolder) {
        if (seite.isVisible(exclusionsService, lang).isVisible()) {
            saveSeiteTo(seite, outputFolder);
            saveSeitenTo(seite.getSeiten(), outputFolder);
            return true;
        }
        return false;
    }
    
    protected abstract void saveSeiteTo(SeiteSO seite, File outputFolder);
    
    private File getFolder(String name) {
        File folder = new File(MinervaWebapp.factory().getWorkFolder("export"), FileService.getSafeName(name));
        FileService.deleteFolder(folder);
        return folder;
    }
}
