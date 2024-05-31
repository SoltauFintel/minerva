package minerva.preview;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.exclusions.ExclusionsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeiteVisible;
import minerva.seite.Breadcrumb;
import minerva.seite.NavigateService;
import minerva.seite.SPage;
import minerva.seite.TocMacro;

public class PreviewPage extends SPage {
    
    @Override
    protected void execute() {
        String customer = ctx.pathParam("customer");
        String lang = ctx.pathParam("lang");
        Logger.info("preview: " + user.getLogin() + " | " + branch + " | " + customer + " | "
                + lang + " | " + seite.getTitle());

        SeiteVisible v = seite.isVisible(customer, lang);
        if (v.isEmpty()) {
            throw new RuntimeException("Empty page is not part of preview");
        } else if (v.isInvisible()) {
            throw new RuntimeException("Page is not accessible in preview for customer " + esc(customer));
        }
        ExclusionsService sv = v.getExclusionsService();
        
        put("title", esc(seite.getSeite().getTitle().getString(lang)) + " - " + n("preview") + " " + lang.toUpperCase()
                + TITLE_POSTFIX);
        put("titel", esc(seite.getSeite().getTitle().getString(lang)));
        String html = seite.getContent().getString(lang);
        TocMacro toc = new TocMacro(seite.getTocMacroPage(), customer, lang, "");
        put("content", toc.transform(html)); // transform before getTOC
        put("toc", toc.getTOC());

        fillBreadcrumbs(customer, lang, list("breadcrumbs"));
        
        String onlyBookFolder0 = "/p/" + branch + "/" + esc(customer) + "/" + bookFolder + "/" + lang;
        String onlyBookFolder = onlyBookFolder0 + "/";
        NavigateService nav = new NavigateService(true, lang, sv);
        navlink("prevlink", nav.previousPage(seite), id, onlyBookFolder, onlyBookFolder0);
        navlink("nextlink", nav.nextPage(seite), id, onlyBookFolder, null);
        
        put("hasBook", false);
        put("hasPreviewBooks", true);
        DataList list = list("previewBooks");
        for (BookSO b : books) {
            if (b.hasContent(lang, sv) && !b.isFeatureTree() && !b.isInternal()) {
                DataMap map = list.add();
                map.put("title", esc(b.getBook().getTitle().getString(lang)));
                map.put("link", "/p/" + branch + "/" + esc(customer) + "/" + b.getBook().getFolder() + "/" + lang);
            }
        }
    }
    
    // from ViewSeitePage
    private void navlink(String name, SeiteSO nav, String seiteId, String onlyBookFolder, String booklink) {
        String nav_id = nav.getId();
        String hasName = "has" + name.substring(0, 1).toUpperCase() + name.substring(1);
        boolean has = !nav_id.equals(seiteId);
        String link = onlyBookFolder + nav_id;
        if (!has && booklink != null) {
            has = true;
            link = booklink;
        }
        put(hasName, has);
        put(name, link);
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
