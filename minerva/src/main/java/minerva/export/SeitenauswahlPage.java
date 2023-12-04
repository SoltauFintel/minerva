package minerva.export;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.workspace.WPage;

/**
 * Select and then export page(s)
 */
public class SeitenauswahlPage extends WPage {
    private static final String BOOK_PREFIX = "book_";
    
    @Override
    protected void execute() {
        // params see ExportRequest!
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");
        String template = ctx.queryParam("template");
        String o = ctx.queryParam("o");
        String w = ctx.queryParam("w");

        if (isPOST()) {
            String auswahlliste = ctx.formParam("al");
            List<SeiteSO> seiten = getSelectedPages(auswahlliste, lang);
            if (seiten.isEmpty()) {
                throw new RuntimeException("No pages to export!");
            }
            info(lang, customer, seiten);
        
            String id = GenericExportService.getService(new ExportRequest(workspace, ctx)).getSeitenExportDownloadId(seiten);
            
            DownloadExportPage.redirectToThisPage(ctx, branch, id);
        } else {
            header(n("seitenauswahl"));
            put("lang", esc(lang));
            put("customer", esc(customer));
            put("template", esc(template));
            put("o", esc(o));
            put("w", esc(w));
            putInt("size", 30);
            putInt("width", 700);
            put("bookPrefix", BOOK_PREFIX);
            DataList list = list("pages");
            for (BookSO book : workspace.getBooks()) {
                DataMap map = list.add();
                map.put("text", esc(book.getBook().getTitle().getString(lang)));
                map.put("id", BOOK_PREFIX + esc(book.getBook().getFolder()));
                map.put("isBook", true);
                
                add(book.getSeiten(), "____", lang, list);
            }
        }
    }

    private List<SeiteSO> getSelectedPages(String auswahlliste, String lang) {
        List<SeiteSO> seiten = new ArrayList<>();
        if (!StringService.isNullOrEmpty(auswahlliste)) {
            for (String id : auswahlliste.split(",")) {
                if (!id.isEmpty()) {
                    for (BookSO book : workspace.getBooks()) {
                        SeiteSO seite = book._seiteById(id);
                        if (seite != null) {
                            Logger.debug(id + " => " + seite.getSeite().getTitle().getString(lang));
                            seiten.add(seite);
                        }
                    }
                }
            }
        }
        return seiten;
    }

    private void info(String lang, String customer, List<SeiteSO> seiten) {
        String info = branch + " | language: " + lang + " | customer: " + customer + //
                (seiten.size() == 1 ? " | exporting this page:" : " | exporting these pages:");
        for (SeiteSO seite : seiten) {
            info += "\n- " + seite.getSeite().getTitle().getString(lang);
        }
        Logger.info(user.getLogin() + " | " + info);
        user.log(info);
    }
    
    private void add(SeitenSO seiten, String indent, String lang, DataList list) {
        for (SeiteSO seite : seiten) {
            int hc = seite.hasContent(lang);
            if (hc > 0) {
                DataMap map = list.add();
                String title = seite.getSeite().getTitle().getString(lang);
                map.put("text", esc(indent + title));
                map.put("id", esc(seite.getId()));
                map.put("isBook", false);
                
                add(seite.getSeiten(), indent + "____", lang, list); // recursive
            }
        }
    }
}
