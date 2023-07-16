package minerva.export;

import static github.soltaufintel.amalia.web.action.Escaper.esc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.Template;
import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;
import com.github.template72.loader.ResourceTemplateLoader;

import minerva.base.FileService;
import minerva.base.NLS;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeiteVisible;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;

/**
 * Multi-page HTML export
 */
public class MultiPageHtmlExportService extends GenericExportService {
    private final WorkspaceSO workspace;
    
    public MultiPageHtmlExportService(WorkspaceSO workspace, String customer, String language) {
        super(workspace, customer, language);
        this.workspace = workspace;
        workspace.getUser().onlyAdmin();
    }
    
    @Override
    protected void init(File outputFolder) {
        String css = loadTemplate("template.css");
        FileService.savePlainTextFile(new File(outputFolder, "online-help.css"), css);
    }
    
    @Override
    public File saveWorkspace(WorkspaceSO workspace) {
        File outputFolder = super.saveWorkspace(workspace);

        // books overview
        saveIndex(outputFolder, "books.html", getBooksModel(workspace));
        
        return outputFolder;
    }

    private DataMap getBooksModel(WorkspaceSO workspace) {
        DataMap model = new DataMap();
        model.put("title", esc(n("books")));
        model.put("customer", esc(getCustomer()));
        model.put("LANG", esc(lang.toUpperCase()));
        model.put("lang", esc(lang));
        DataList list = model.list("books");
        for (BookSO book : workspace.getBooks()) {
            DataMap map = list.add();
            map.put("link", esc(book.getBook().getFolder()) + "/index.html");
            map.put("title", esc(book.getBook().getTitle().getString(lang)));
        }
        return model;
    }
    
    @Override
    protected void saveBookTo(BookSO book, File outputFolder) {
        // Gliederung
        saveIndex(outputFolder, "gliederung.html", getGliederungModel(book));

        super.saveBookTo(book, outputFolder);
    }

    private DataMap getGliederungModel(BookSO book) {
        DataMap model = new DataMap();
        String title = book.getBook().getTitle().getString(lang);
        model.put("title", esc(title));
        model.put("customer", esc(getCustomer()));
        model.put("LANG", esc(lang.toUpperCase()));
        model.put("lang", esc(lang));
        StringBuilder gliederung = new StringBuilder();
        addSeiten(book.getSeiten(), gliederung);
        model.put("gliederung", gliederung.toString());
        return model;
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
        int o = html.indexOf("<body>");
        int oo = html.indexOf("</body>");
        String body = html.substring(o + "<body>".length(), oo);
        
        DataMap model = new DataMap();
        model.put("title", esc(title));
        model.put("content", body); // no esc!
        html = render(loadTemplate("page.html"), model);
        
        // TODO previous page, parent page, next page, breadcrumbs, to book page 
        
        List<Link> links = LinkService.extractLinks(html, false);
        for (Link link : links) {
            if (!(link.getHref().startsWith("http://") || link.getHref().startsWith("https://"))) {
                html = html.replace("<a href=\"" + link.getHref() + "\">", "<a href=\"" + link.getHref() + ".html\">");
            }
        }
        
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
    
    private void saveIndex(File outputFolder, String dn, DataMap model) {
        FileService.savePlainTextFile(new File(outputFolder, "index.html"),
                render(loadTemplate(dn), model));
    }
    
    private String render(String template, DataMap model) {
        DataMap model2 = new DataMap();
        model2.put("title", model.get("title").toString());
        model2.put("tcontent", Template.createFromString(template).withData(model).render());
        return Template.createFromString(loadTemplate("template.html")).withData(model2).render();
    }

    private String loadTemplate(String dn) {
        File file = new File(workspace.getFolder(), dn);
        if (file.isFile()) {
            Logger.debug("export: using template " + dn + " from workspace " + workspace.getBranch());
            return FileService.loadPlainTextFile(file);
        }
        Logger.debug("export: using built-in template " + dn);
        return loadBuiltInTemplate(dn); // fallback
    }
    
    private String loadBuiltInTemplate(String dn) {
        return ResourceTemplateLoader.loadResource(getClass(), "/templates/export/" + dn, "UTF-8");
    }
    
    private String n(String key) {
        return NLS.get(lang, key);
    }
}
