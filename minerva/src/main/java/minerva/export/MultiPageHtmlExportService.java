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
import minerva.seite.NavigateService;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;

/**
 * Multi-page HTML export
 */
public class MultiPageHtmlExportService extends GenericExportService {
    private final WorkspaceSO workspace;
    private int counter = 0;
    
    public MultiPageHtmlExportService(WorkspaceSO workspace, String customer, String language) {
        super(workspace, customer, language);
        this.workspace = workspace;
        workspace.getUser().onlyWithExportRight();
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
        std(n("books"), model);
        DataList list = model.list("books");
        for (BookSO book : workspace.getBooks()) {
            DataMap map = list.add();
            map.put("link", esc(book.getBook().getFolder()) + "/index.html");
            map.put("title", esc(book.getBook().getTitle().getString(lang)));
        }
        model.put("cssFolder", esc(FileService.getSafeName(
                workspace.getBooks().get(0).getBook().getFolder())
                + "/html/"));
        return model;
    }
    
    @Override
    protected void saveBookTo(BookSO book, File outputFolder) {
        // Gliederung
        saveIndex(outputFolder, "book.html", getBookModel(book));

        super.saveBookTo(book, outputFolder);
    }

    private DataMap getBookModel(BookSO book) {
        DataMap model = new DataMap();
        std(book.getBook().getTitle().getString(lang), model);
        navigationBooksMode(model, ".html", "");
        StringBuilder outline = new StringBuilder();
        addSeiten(book.getSeiten(), outline);
        model.put("outline", outline.toString());
        model.put("cssFolder", "html/");
        return model;
    }
    
    private void std(String title, DataMap model) {
        model.put("title", esc(title));
        String customer = getCustomer();
        model.put("customer", esc(customer));
        model.put("hasCustomer", !"-".equals(customer));
        model.put("LANG", esc(lang.toUpperCase()));
        model.put("lang", esc(lang.toLowerCase()));
        model.put("back", n("back"));
        model.put("forward", n("forward"));
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
                html.append("\n<li><a href=\"html/" + esc(seite.getId()) + ".html\""
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
        currentBook = null;
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
    protected void saveSeiteTo(SeiteSO seite, SeiteSO parent, File outputFolder) {
        String html = seite.getContent().getString(lang);
        String title = seite.getSeite().getTitle().getString(lang);
        DataMap model = new DataMap();
        model.put("title", esc(title));
        model.put("content", getBody(html, title)); // no esc!
        model.put("cssFolder", "");
        model.put("back", n("back"));
        model.put("forward", n("forward"));
        navigation(seite, parent, model);
        html = render(loadTemplate("page.html"), model);
        html = addDotHtml(html);
        
        // formulas to images
        Formula2Image c = new Formula2Image();
        c.setCounter(counter);
        c.setPath("img/" + seite.getId() + "/");
        html = c.processHTML(html, "\\[", "\\]", outputFolder, seite, "<p class=\"math\">", "</p>", title);
        html = c.processHTML(html, "\\(", "\\)", outputFolder, seite, "", "", title);
        counter = c.getCounter();
        
        // HTML file
        FileService.savePlainTextFile(new File(outputFolder, seite.getId() + ".html"), html);
        
        // images
        FileService.copyFiles(
                new File(seite.getBook().getFolder() + "/img/" + seite.getId()),
                new File(outputFolder, "img/" + seite.getId()));
    }

    private String getBody(String html, String title) {
        String body;
        int o = html.indexOf("<body>");
        if (o >= 0) {
            o += "<body>".length();
            int oo = html.indexOf("</body>");
            if (oo < 0) {
                throw new RuntimeException("</body> not found for page " + title);
            }
            body = html.substring(o, oo);
        } else {
            Logger.warn("Page \"" + title + "\" (" + lang + ") has no <body>! Use whole content.");
            body = html;
        }
        return body;
    }

    private String addDotHtml(String html) {
        List<Link> links = LinkService.extractLinks(html, false);
        for (Link link : links) {
            if (!(link.getHref().startsWith("http://") || link.getHref().startsWith("https://"))) {
                html = html.replace("<a href=\"" + link.getHref() + "\">", "<a href=\"" + link.getHref() + ".html\">");
            }
        }
        return html;
    }
    
    private void navigation(SeiteSO seite, SeiteSO parent, DataMap model) {
        NavigateService nav = new NavigateService(true, lang, exclusionsService);
        nav.setSortAllowed(false);
        SeiteSO bb = nav.previousPage(seite);
        boolean b = bb != null && bb != seite;
        model.put("hasPrevLink", b);
        if (b) {
            model.put("prevLink", bb.getId());
        }
        
        bb = nav.nextPage(seite);
        b = bb != null && bb != seite;
        model.put("hasNextLink", b);
        if (b) {
            model.put("nextLink", bb.getId());
        }

        model.put("hasParentLink", parent != null);
        if (parent != null) {
            model.put("parentLink", parent.getId());
            model.put("parentTitle", esc(parent.getSeite().getTitle().getString(lang)));
        }
        
        model.put("hasBookLink", currentBook != null);
        if (currentBook != null) {
            model.put("bookLink", "../index");
            model.put("bookTitle", esc(currentBook.getBook().getTitle().getString(lang)));
        }
        
        navigationBooksMode(model, "", "../");
    }

    private void navigationBooksMode(DataMap model, String postfix, String path) {
        model.put("booksMode", booksMode);
        if (booksMode) {
            model.put("booksModeLink", path + "../index" + postfix);
            model.put("booksModeTitle", n("booksModeTitle"));
        }
    }
    
    private void saveIndex(File outputFolder, String dn, DataMap model) {
        FileService.savePlainTextFile(new File(outputFolder, "index.html"),
                render(loadTemplate(dn), model));
    }
    
    private String render(String template, DataMap model) {
        DataMap model2 = new DataMap();
        model2.put("title", model.get("title").toString());
        model2.put("tcontent", Template.createFromString(template).withData(model).render());
        model2.put("cssFolder", model.get("cssFolder").toString());
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
