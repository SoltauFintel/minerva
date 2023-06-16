package minerva.preview;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class PreviewBookPage extends BPage {

    @Override
    protected void execute() {
        String customer = ctx.pathParam("customer");
        String lang = ctx.pathParam("lang");
        Logger.info(user.getUser().getLogin() + " | " + branch + " | " + customer + " | "
                + lang + " | book: " + book.getBook().getFolder());

        put("title", esc(book.getBook().getTitle().getString(lang))
                + " - " + n("preview") + " " + lang.toUpperCase() + TITLE_POSTFIX);
        put("titel", esc(book.getBook().getTitle().getString(lang)));
        put("hasPrevlink", false);
        put("hasNextlink", false);
        
        DataList list = list("books");
        for (BookSO b : books) {
            DataMap map = list.add();
            map.put("title", esc(b.getBook().getTitle().getString(lang)));
            map.put("link", "/p/" + branch + "/" + esc(customer) + "/" + b.getBook().getFolder() + "/" + lang);
        }
        
        long start = System.currentTimeMillis();
        StringBuilder gliederung = new StringBuilder();
        fillSeiten(branch, customer, bookFolder, book.getSeiten(), lang, gliederung, book.getBook().isSorted());
        put("gliederung", gliederung.toString());
        Logger.debug("Preview gliederung " + (System.currentTimeMillis() - start) + "ms");
    }

    private void fillSeiten(String branch, String customer, String bookFolder, SeitenSO seiten, String lang, StringBuilder gliederung,
            boolean sorted) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        for (SeiteSO seite : seiten) {
            int hasContent = seite.hasContent(lang);
            if (hasContent > 0) {
                String title = esc(seite.getSeite().getTitle().getString(lang));
                String link = "/p/" + branch + "/" + esc(customer) + "/" + bookFolder + "/" + lang + "/"
                        + esc(seite.getSeite().getId());
                String nc = hasContent == 2 ? " class=\"noContent\"" : "";
                gliederung.append("\t<li id=\"");
                gliederung.append(seite.getId());
                gliederung.append("\"><a href=\"");
                gliederung.append(link);
                gliederung.append("\"");
                gliederung.append(nc);
                gliederung.append(">");
                gliederung.append(title);
                gliederung.append("</a></li>\n");
                
                fillSeiten(branch, customer, bookFolder, seite.getSeiten(), lang, gliederung, true); // recursive
            }
        }
        gliederung.append("</ul>\n");
    }
}
