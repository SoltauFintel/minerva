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

public class SeitenauswahlPage extends WPage {
    private static final String BOOK_PREFIX = "book_";
    
    @Override
    protected void execute() {
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");
        String template = ctx.queryParam("template");
        String o = ctx.queryParam("o");
        String w = ctx.queryParam("w");

        if (isPOST()) {
            String al = ctx.formParam("al");
            List<SeiteSO> seiten = new ArrayList<>();
            if (!StringService.isNullOrEmpty(al)) {
                for (String i : al.split(",")) {
                    if (!i.isEmpty()) {
                        for (BookSO book : workspace.getBooks()) {
                            SeiteSO x = book._seiteById(i);
                            if (x != null) {
                                Logger.debug(i + " => " + x.getSeite().getTitle().getString(lang));
                                seiten.add(x);
                            }
                        }
                    }
                }
            }
            // TODO Baustelle
            ctx.redirect("/w/" + esc(branch) + "/menu");
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
    
    private void add(SeitenSO seiten, String indent, String lang, DataList list) {
        for (SeiteSO seite : seiten) {
            DataMap map = list.add();
            String title = seite.getSeite().getTitle().getString(lang);
            map.put("text", esc(indent + title));
            map.put("id", esc(seite.getId()));
            map.put("isBook", false);
            
            add(seite.getSeiten(), indent + "____", lang, list); // recursive
        }
    }
}
