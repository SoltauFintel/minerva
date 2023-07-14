package minerva.export;

import java.io.File;

import minerva.base.FileService;
import minerva.exclusions.Exclusions;
import minerva.exclusions.ExclusionsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;

/**
 * Multi-page HTML export
 */
public class ExportService {
    /** language, e.g. "en" */
    private final String lang;
    private ExclusionsService exclusionsService;

    public ExportService(String lang) {
        this.lang = lang;
    }

    public void initExclusionsService(WorkspaceSO workspace, String customer) {
        exclusionsService = new ExclusionsService();
        exclusionsService.setCustomer(customer);
        exclusionsService.setExclusions(new Exclusions(workspace.getExclusions().get()));
    }
    
    public void save(WorkspaceSO workspace, File targetFolder) {
        // TODO Bücher Seite
        for (BookSO book : workspace.getBooks()) {
            save(book, targetFolder);
        }
    }

    public void save(BookSO book, File targetFolder) {
        // TODO Gliederung
        save(book.getSeiten(lang), new File(targetFolder, book.getBook().getFolder()));
    }
    
    public void save(SeitenSO seiten, File targetFolder) {
        for (SeiteSO seite : seiten) {
            save(seite, targetFolder);
        }
    }
    
    public void save(SeiteSO seite, File targetFolder) {
        // TODO paging
        // TODO Links: append ".html"
        // TODO formulas
        FileService.savePlainTextFile(new File(targetFolder, seite.getId() + ".html"),
                seite.getContent().getString(lang));
        // TODO images
        save(seite.getSeiten(lang), targetFolder); // recursive
    }
}
