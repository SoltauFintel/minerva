package minerva.preview;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.exclusions.Exclusions;
import minerva.exclusions.ExclusionsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.Breadcrumb;
import minerva.seite.NavigateService;
import minerva.seite.SPage;

public class PreviewPage extends SPage {
    
    @Override
    protected void execute() {
        String customer = ctx.pathParam("customer");
        String lang = ctx.pathParam("lang");
        Logger.info("preview: " + user.getUser().getLogin() + " | " + branch + " | " + customer + " | "
                + lang + " | " + seite.getTitle());

        if (seite.hasContent(lang) == 0) {
            throw new RuntimeException("Empty page is not part of preview");
        }
        ExclusionsService sv = new ExclusionsService();
        sv.setExclusions(new Exclusions(book.getWorkspace().getExclusions().get()));
        sv.setCustomer(customer);
        sv.setSeite(seite);
        if (!sv.isAccessible()) {
            throw new RuntimeException("Page is not accessible in preview for customer " + esc(customer));
        }
        
        put("title", esc(seite.getSeite().getTitle().getString(lang)) + " - " + n("preview") + " " + lang.toUpperCase()
                + TITLE_POSTFIX);
        put("titel", esc(seite.getSeite().getTitle().getString(lang)));
        put("content", seite.getContent().getString(lang));

        fillBreadcrumbs(customer, lang, list("breadcrumbs"));
        
        String onlyBookFolder = "/p/" + branch + "/" + esc(customer) + "/" + bookFolder + "/" + lang + "/";
        NavigateService nav = new NavigateService(true, lang, sv);
        navlink("prevlink", nav.previousPage(seite), id, onlyBookFolder);
        navlink("nextlink", nav.nextPage(seite), id, onlyBookFolder);
        
        DataList list = list("books");
        for (BookSO b : books) {
            DataMap map = list.add();
            map.put("title", esc(b.getBook().getTitle().getString(lang)));
            map.put("link", "/p/" + branch + "/" + esc(customer) + "/" + b.getBook().getFolder() + "/" + lang);
        }
    }
    
    // from ViewSeitePage
    private void navlink(String name, SeiteSO nav, String seiteId, String onlyBookFolder) {
        String nav_id = nav.getId();
        String has = "has" + name.substring(0, 1).toUpperCase() + name.substring(1);
        put(has, !nav_id.equals(seiteId));
        put(name, onlyBookFolder + nav_id);
    }

    private void fillBreadcrumbs(String customer, String lang, DataList list) {
        List<Breadcrumb> breadcrumbs = book.getBreadcrumbs(id, new PreviewAreaBreadcrumbLinkBuilder(customer, lang));
        for (int i = breadcrumbs.size() - 1; i >= 0; i--) {
            Breadcrumb b = breadcrumbs.get(i);
            DataMap map = list.add();
            map.put("title", esc(b.getTitle().getString(lang)));
            map.put("link", b.getLink());
            map.put("first", i == breadcrumbs.size() - 1);
            map.put("last", i == 0);
        }
    }
}
