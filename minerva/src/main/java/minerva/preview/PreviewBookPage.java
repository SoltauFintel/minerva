package minerva.preview;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.exclusions.Exclusions;
import minerva.exclusions.ExclusionsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.SeiteVisible;

public class PreviewBookPage extends BPage {

    @Override
    protected void execute() {
        String customer = ctx.pathParam("customer");
        String lang = ctx.pathParam("lang");
        Logger.info(user.getLogin() + " | " + branch + " | " + customer + " | "
                + lang + " | book: " + book.getBook().getFolder());

        ExclusionsService sv = new ExclusionsService();
        sv.setExclusions(new Exclusions(book.getWorkspace().getExclusions().get()));
        sv.setCustomer(customer);

        setJQueryObenPageMode();
        put("title", esc(book.getBook().getTitle().getString(lang))
                + " - " + n("preview") + " " + lang.toUpperCase() + TITLE_POSTFIX);
        put("titel", esc(book.getBook().getTitle().getString(lang)));
        put("hasBook", false);
        put("hasPreviewBooks", true);
        put("hasPrevlink", false);
        boolean hasSeiten = !book.getSeiten().isEmpty();
        put("hasNextlink", hasSeiten);
        String linkPrefix = "/p/" + branch + "/" + esc(customer) + "/";
        if (hasSeiten) {
            String id = book.getSeiten().get(0).getId();
            put("nextlink", linkPrefix + book.getBook().getFolder() + "/" + lang + "/" + id);
        }
        
        DataList list = list("previewBooks");
        for (BookSO b : books) {
            if (b.hasContent(lang, sv) && !b.isFeatureTree() && !b.isInternal()) {
                DataMap map = list.add();
                map.put("title", esc(b.getBook().getTitle().getString(lang)));
                map.put("link", linkPrefix + b.getBook().getFolder() + "/" + lang);
            }
        }
        
        long start = System.currentTimeMillis();
        StringBuilder gliederung = new StringBuilder();
        fillSeiten(branch, customer, bookFolder, book.getSeiten(), lang, sv, book.getBook().isSorted(), gliederung);
        put("gliederung", gliederung.toString());
        Logger.debug("Preview gliederung " + (System.currentTimeMillis() - start) + "ms");
    }

    private void fillSeiten(String branch, String customer, String bookFolder, SeitenSO seiten, String lang,
            ExclusionsService sv, boolean sorted, StringBuilder gliederung) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        for (SeiteSO seite : seiten) {
            SeiteVisible v = seite.isVisible(sv, lang);
            if (v.isVisible() && !seite.isNoTree()) {
                String title = esc(seite.getSeite().getTitle().getString(lang));
                String link = "/p/" + branch + "/" + esc(customer) + "/" + bookFolder + "/" + lang + "/"
                        + esc(seite.getSeite().getId());
                String nc = v.hasSubpages() ? " class=\"noContent\"" : "";
                gliederung.append("\t<li id=\"");
                gliederung.append(seite.getId());
                gliederung.append("\"><a href=\"");
                gliederung.append(link);
                gliederung.append("\"");
                gliederung.append(nc);
                gliederung.append(">");
                gliederung.append(title);
                gliederung.append("</a></li>\n");
                
                fillSeiten(branch, customer, bookFolder, seite.getSeiten(), lang, sv, true, gliederung); // recursive
            }
        }
        gliederung.append("</ul>\n");
    }
}
