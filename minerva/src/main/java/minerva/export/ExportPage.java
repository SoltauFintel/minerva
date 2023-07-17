package minerva.export;

import static minerva.base.StringService.umlaute;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;
import minerva.workspace.WPage;

public class ExportPage extends WPage {
    // TODO Die letzte Einstellung merken.
    private static final String W = "###";
    private static final String B = "##";
    private static final String S = "#";
    private String lang;
    
    @Override
    protected void execute() {
        user.onlyAdmin();
        if (workspace.getBooks().isEmpty()) {
            throw new RuntimeException("There are no books!");
        }
        lang = user.getGuiLanguage();

        if (isPOST()) {
            callExportDownload();
        } else {
            header(n("export"));
            List<String> items = getItems(workspace);
            List<String> customers = new ArrayList<>(workspace.getExclusions().getCustomers());
            customers.add(0, "-");
            
            combobox("items", items, items.get(1), false, model);
            combobox("customers", customers, "-", false, model);  // TODO uppercase
            combobox("langs", langs, langs.get(0), false, model); // TODO uppercase
            // Denkbar wäre hier noch die Wahl eines Template-Sets und/oder einer CSS-Datei.
            
            ColumnFormularGenerator gen = new ColumnFormularGenerator(2, 1);
            gen.save(n("export")).cancel(n("cancel"));
            TemplatesInitializer.fp.setContent(gen
                    .combobox("items", n("exportWhat") + "?", 7, "items")
                    .combobox("customers", n("customer"), 2, "customers")
                    .combobox("langs", n("language"), 1, "langs")
                    .getHTML("/w/" + branch + "/export-what", "/b/" + branch));
        }
    }
    
    private List<String> getItems(WorkspaceSO workspace) {
        List<String> items = new ArrayList<>();
        List<String> pageTitles = new ArrayList<>();
        items.add(n("allBooks") + " " + W);
        for (BookSO book : workspace.getBooks()) {
            items.add(book.getBook().getTitle().getString(lang) + " " + B + book.getBook().getFolder());
            addPageTitles(book.getSeiten(), pageTitles);
        }
        pageTitles.sort((a, b) -> umlaute(a).compareTo(umlaute(b)));
        items.addAll(pageTitles);
        return items;
    }

    private void addPageTitles(SeitenSO seiten, List<String> items) {
        for (SeiteSO seite : seiten) {
            items.add(seite.getSeite().getTitle().getString(lang) + " " + S + seite.getId());
            addPageTitles(seite.getSeiten(), items);
        }
    }
    
    @Override
    protected String getPage() {
        return "formular/" + super.getPage();
    }

    private void callExportDownload() {
        String item = ctx.queryParam("items");
        String lang = ctx.queryParam("langs");
        String customer = ctx.queryParam("customers");
        String q = "/export?lang=" + u(lang) + "&customer=" + u(customer);

        if (item.contains(W)) { // all books
            ctx.redirect("/w/" + branch + q);
            
        } else if (item.contains(B)) { // book
            String bookFolder = item.substring(item.lastIndexOf(B) + B.length());
            ctx.redirect("/b/" + branch + "/" + esc(bookFolder) + q);
            
        } else if (item.contains(S)) { // Seite
            String seiteId = item.substring(item.lastIndexOf(S) + S.length());
            SeiteSO seite = getSeite(seiteId);
            if (seite != null) {
                String bookFolder = seite.getBook().getBook().getFolder();
                ctx.redirect("/s/" + branch + "/" + esc(bookFolder) + "/" + seiteId + q);
            } else {
                Logger.error("Page not found for export item: " + item);
                throw new RuntimeException("Page not found");
            }
            
        } else {
            Logger.error("Unknown export item: " + item);
            throw new RuntimeException("Unknown item");
        }
    }
    
    private SeiteSO getSeite(String seiteId) {
        for (BookSO book : workspace.getBooks()) {
            SeiteSO seite = book.getSeiten()._byId(seiteId);
            if (seite != null) {
                return seite;
            }
        }
        return null;
    }
}
