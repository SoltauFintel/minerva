package minerva.export;

import static github.soltaufintel.amalia.web.action.Escaper.esc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.base.NLS;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeiteVisible;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;

/**
 * Multi-page HTML export
 */
public class ExportService extends GenericExportService {

    public ExportService(WorkspaceSO workspace, String customer, String language) {
        super(workspace, customer, language);
        workspace.getUser().onlyAdmin();
    }

    @Override
    public File saveWorkspace(WorkspaceSO workspace) {
        File outputFolder = super.saveWorkspace(workspace);

        // books overview
        // TODO template
        String title = n("books");
        String html = "<h1>" + esc(title) + "</h1><h2>" + esc(getCustomer()) + " / " + esc(lang) + "</h2><ul>";
        for (BookSO book : workspace.getBooks()) {
            html += "\n<li><a href=\"" + esc(book.getBook().getFolder()) + "/index.html\">"
                    + esc(book.getBook().getTitle().getString(lang)) + "</a></li>";
        }
        html += "</ul>";
        index(outputFolder, title, html);
        
        return outputFolder;
    }
    
    @Override
    protected void saveBookTo(BookSO book, File outputFolder) {
        // Gliederung
        // TODO template
        String title = esc(book.getBook().getTitle().getString(lang));
        StringBuilder html = new StringBuilder();
        html.append("<h1>" + esc(title) + "</h1><h2>" + esc(getCustomer()) + " / " + esc(lang) + "</h2>");
        addSeiten(book.getSeiten(), html);
        index(outputFolder, title, html.toString());

        super.saveBookTo(book, outputFolder);
    }
    
    private void addSeiten(SeitenSO seiten, StringBuilder html) {
        boolean first = true;
        for (SeiteSO seite : seiten) {
            SeiteVisible v = seite.isVisible(exclusionsService, lang);
            if (v.isVisible()) {
                if (first) {
                    html.append("<ul>");
                    first = false;
                }
                html.append("\n<li><a href=\"" + esc(seite.getId()) + ".html\""
                        + (v.hasSubpages() ? " class=\"noContent\"" : "") + ">"
                        + esc(seite.getSeite().getTitle().getString(lang)) + "</a>");
                addSeiten(seite.getSeiten(), html);
                html.append("</li>");
            }
        }
        if (!first) {
            html.append("</ul>");
        }
    }

    @Override
    public File saveSeite(SeiteSO seite) {
        File outputFolder = super.saveSeite(seite);

        // copy main file as index.html as entry point
        File src = new File(outputFolder, seite.getId() + ".html");
        File t = new File(outputFolder, "index.html");
        try {
            Files.copy(src.toPath(), t.toPath());
        } catch (IOException e) {
            Logger.error(e, "Error creating index.html for " + seite.getId() + ".html");
        }
        return outputFolder;
    }
    
    @Override
    protected void saveSeiteTo(SeiteSO seite, File outputFolder) {
        String html = seite.getContent().getString(lang);

        // page title
        String title = seite.getSeite().getTitle().getString(lang);
        html = html.replace("<head></head>", "<head><title>" + esc(title) + "</title></head>")
                .replace("<body>", "<body><h1>" + esc(title) + "</h1>");
        
        // TODO paging (Luxus)
        // TODO Links: append ".html"
        // TODO formulas to images   \(...\)    \[...\]
        
        // HTML file
        FileService.savePlainTextFile(new File(outputFolder, seite.getId() + ".html"), html);
        
        // images
        FileService.copyFiles(
                new File(seite.getBook().getFolder() + "/img/" + seite.getId()),
                new File(outputFolder, "img/" + seite.getId()));

        // subpages
        saveSeitenTo(seite.getSeiten(), outputFolder); // recursive
    }
    
    private String n(String key) {
        return NLS.get(lang, key);
    }
    
    private void index(File outputFolder, String title, String content) {
        FileService.savePlainTextFile(new File(outputFolder, "index.html"),
                "<html><head><title>" + esc(title)
                        + "</title><link rel=\"stylesheet\" type=\"text/css\" href=\"online-help.css\"></head><body>\n"
                        + content + "\n</body></html>\n");
    }
}
