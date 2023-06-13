package minerva.preview;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.Breadcrumb;
import minerva.seite.NavigateService;
import minerva.seite.SPage;

public class PreviewPage extends SPage {
    
    @Override
    protected void execute() {
        String lang = ctx.pathParam("lang");
        Logger.info(user.getUser().getLogin() + " | " + branch + " | " + lang + " | " + seite.getTitle());

        put("title", esc(seite.getSeite().getTitle().getString(lang)) + " - " + n("preview") + " " + lang.toUpperCase()
                + TITLE_POSTFIX);
        put("titel", esc(seite.getSeite().getTitle().getString(lang)));
        put("content", seite.getContent().getString(lang));

        fillBreadcrumbs(lang, list("breadcrumbs"));
        
        String onlyBookFolder = "/p/" + branch + "/" + bookFolder + "/" + lang + "/";
        NavigateService nav = new NavigateService();
        navlink("prevlink", nav.previousPage(seite), id, onlyBookFolder);
        navlink("nextlink", nav.nextPage(seite), id, onlyBookFolder);
        
        DataList list = list("books");
        for (BookSO b : books) {
            DataMap map = list.add();
            map.put("title", esc(b.getBook().getTitle().getString(lang)));
            map.put("link", "/p/" + branch + "/" + b.getBook().getFolder() + "/" + lang);
        }
    }
    
    // from ViewSeitePage
    private void navlink(String name, SeiteSO nav, String seiteId, String onlyBookFolder) {
        String nav_id = nav.getId();
        String has = "has" + name.substring(0, 1).toUpperCase() + name.substring(1);
        put(has, !nav_id.equals(seiteId));
        put(name, onlyBookFolder + nav_id);
    }

    private void fillBreadcrumbs(String lang, DataList list) {
        List<Breadcrumb> breadcrumbs = book.getBreadcrumbs(id);
        for (int i = breadcrumbs.size() - 1; i >= 0; i--) {
            Breadcrumb b = breadcrumbs.get(i);
            DataMap map = list.add();
            map.put("title", esc(b.getTitle().getString(lang)));
            String link = b.getLink();
            if (link.startsWith("/b/")) { // book link
                link = link.replace("/b/", "/p/") + "/" + lang;
            } else { // page link
                link = link.replace("/s/", "/p/");
                int o = link.lastIndexOf("/");
                link = link.substring(0, o) + "/" + lang + link.substring(o);
            }
            map.put("link", link);
            map.put("first", i == breadcrumbs.size() - 1);
            map.put("last", i == 0);
        }
    }
}
