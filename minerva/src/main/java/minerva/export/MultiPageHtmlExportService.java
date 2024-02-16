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

import minerva.base.FileService;
import minerva.base.NLS;
import minerva.export.Formula2Image.TransformPath;
import minerva.export.pdf.Chapter;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeiteVisible;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;
import minerva.seite.NavigateService;
import minerva.seite.TocMacro;
import minerva.seite.TocMacroPage;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;

/**
 * Multi-page HTML export
 */
public class MultiPageHtmlExportService extends GenericExportService {
    private int counter = 0;
    private List<SeiteSO> validPages = null;
    
    public MultiPageHtmlExportService(ExportRequest req) {
        super(req);
        req.getWorkspace().getUser().onlyWithExportRight();
        exclusionsService.setContext("Multi-page-HTML-export");
    }
    
    @Override
    protected void init(File outputFolder) {
        FileService.savePlainTextFile(new File(outputFolder, "online-help.css"), exportTemplateSet.getStyles());
    }
    
    @Override
    public File saveWorkspace(WorkspaceSO workspace) {
        File outputFolder = super.saveWorkspace(workspace);

        // books overview
        saveIndex(outputFolder, exportTemplateSet.getBooks(), getBooksModel(workspace));
        
        return outputFolder;
    }

    private DataMap getBooksModel(WorkspaceSO workspace) {
        DataMap model = new DataMap();
        std(n("books"), model);
        DataList list = model.list("books");
        for (BookSO book : workspace.getBooks()) {
            if (book.hasContent(lang, exclusionsService)) {
                DataMap map = list.add();
                map.put("link", esc(book.getBook().getFolder()) + "/index.html");
                map.put("title", esc(book.getBook().getTitle().getString(lang)));
            }
        }
        model.put("cssFolder", esc(FileService.getSafeName(
                workspace.getBooks().get(0).getBook().getFolder())
                + "/html/"));
        return model;
    }
    
    @Override
    protected void saveBookTo(BookSO book, File outputFolder) {
        // Gliederung
        saveIndex(outputFolder, exportTemplateSet.getBook(), getBookModel(book));

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
    public File saveSeiten(List<SeiteSO> seiten) {
        currentBook = null;
        validPages = seiten;
        File outputFolder = super.saveSeiten(seiten);

        copyPageFileAsIndexFile(seiten.get(0), outputFolder);
        return outputFolder;
    }
    
    protected void copyPageFileAsIndexFile(SeiteSO seite, File outputFolder) {
        // copy main file as index.html as entry point
        File src = new File(outputFolder, seite.getId() + ".html");
        File dest = new File(outputFolder, "index.html");
        try {
            Files.copy(src.toPath(), dest.toPath());
        } catch (IOException e) {
            Logger.error(e, "Error creating index.html for " + seite.getId() + ".html\nsrc : "
                    + src.getAbsolutePath() + "\ndest: " + dest.getAbsolutePath());
        }
    }
    
    @Override
    protected void saveSeiteTo(SeiteSO seite, SeiteSO parent, Chapter chapter, File outputFolder) {
        String html = seite.getContent().getString(lang);
        String title = seite.getSeite().getTitle().getString(lang);
        DataMap model = new DataMap();
        model.put("title", esc(title));
        String body = getBody(html, title);
        model.put("content", tocMacro(body, seite, exclusionsService.getCustomer())); // no esc!
        model.put("cssFolder", "");
        model.put("back", n("back"));
        model.put("forward", n("forward"));
        String subpages = subpages(seite);
        model.put("subpages", subpages);
        model.put("hasSubpages", !subpages.isEmpty());
        navigation(seite, parent, model);
        html = render(exportTemplateSet.getPage(), model);
        html = addDotHtml(html);
        html = formulas2images(html, seite, outputFolder, title);
        
        // HTML file
        FileService.savePlainTextFile(new File(outputFolder, seite.getId() + ".html"), html);
        
        // images
        FileService.copyFiles(
                new File(seite.getBook().getFolder() + "/img/" + seite.getId()),
                new File(outputFolder, "img/" + seite.getId()));
    }

    private String tocMacro(String html, SeiteSO seite, String customer) {
        TocMacroPage page = seite.getTocMacroPage(false); /* Export has subpages TOC at page end, so turn it off here.
        However, I'm not sure about this here because tocSubpagesLevels is an explicit setting. */
        TocMacro toc = new TocMacro(page, customer, lang, "");
        html = toc.transform(html);
        return toc.getTOC() + html;
    }

    protected final String getBody(String html, String title) {
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

    private String subpages(SeiteSO seite) {
        String ret = "";
        for (SeiteSO sub : seite.getSeiten()) {
            if (sub.isVisible(exclusionsService, lang).isVisible()) {
                if (validPages == null || validPages.contains(sub)) {
                    ret += "<li><a href=\"" + sub.getId() + "\">" + esc(sub.getSeite().getTitle().getString(lang))
                            + "</a></li>";
                }
            }
        }
        return ret.isEmpty() ? ret : "<div class=\"subpages\"><ul>" + ret + "</ul></div>";
    }

    protected final String addDotHtml(String html) {
        List<Link> links = LinkService.extractLinks(html, false);
        for (Link link : links) {
            String href = link.getHref();
            if (!href.startsWith("http://") && !href.startsWith("https://")) {
                String newHref;
                int o = href.lastIndexOf("#");
                if (o >= 0) {
                    String li = href.substring(0, o);
                    String re = href.substring(o);
                    if (li.endsWith(".html")) {
                        continue; // replace not necessary
                    } else {
                        newHref = li + ".html" + re;
                    }
                } else {
                    newHref = href + ".html";
                }
                html = html.replace("<a href=\"" + href + "\">", "<a href=\"" + newHref + "\">");
            }
        }
        return html;
    }
    
    private void navigation(SeiteSO seite, SeiteSO parent, DataMap model) {
        NavigateService nav = new NavigateService(true, lang, exclusionsService);
        nav.setSortAllowed(false);
        nav.setValidPages(validPages);
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
    
    protected final String formulas2images(String html, SeiteSO seite, File outputFolder, String title) {
        Formula2Image c = new Formula2Image();
        c.setCounter(counter);
        c.setPath("img/" + seite.getId() + "/");
        TransformPath tp = getTransformPath();
        html = c.processHTML(html, "\\[", "\\]", outputFolder, seite, "<p class=\"math\">", "</p>", title, tp);
        html = c.processHTML(html, "\\(", "\\)", outputFolder, seite, "", "", title, tp);
        counter = c.getCounter();
        return html;
    }
    
    protected TransformPath getTransformPath() {
        return (path, file) -> path + file.getName();
    }
    
    protected void saveIndex(File outputFolder, String aTemplate, DataMap model) {
        FileService.savePlainTextFile(new File(outputFolder, "index.html"), render(aTemplate, model));
    }
    
    private String render(String aTemplate, DataMap model) {
        DataMap model2 = new DataMap();
        model2.put("title", model.get("title").toString());
        model2.put("content", Template.createFromString(aTemplate).withData(model).render());
        model2.put("cssFolder", model.get("cssFolder").toString());
        return Template.createFromString(exportTemplateSet.getTemplate()).withData(model2).render();
    }

    private String n(String key) {
        return NLS.get(lang, key);
    }
}
