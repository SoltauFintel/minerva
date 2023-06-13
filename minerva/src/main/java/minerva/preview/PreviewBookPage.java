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
        String lang = ctx.pathParam("lang");
        Logger.info(user.getUser().getLogin() + " | " + branch + " | " + lang
                + " | book: " + book.getBook().getFolder());

        put("title", esc(book.getBook().getTitle().getString(lang))
                + " - " + n("preview") + " " + lang.toUpperCase() + TITLE_POSTFIX);
        put("titel", esc(book.getBook().getTitle().getString(lang)));
        put("hasPrevlink", false);
        put("hasNextlink", false);

        StringBuilder gliederung = new StringBuilder();
        fillSeiten(branch, bookFolder, book.getSeiten(), lang, gliederung, book.getBook().isSorted());
        put("gliederung", gliederung.toString());
        
        DataList list = list("books");
        for (BookSO b : books) {
            DataMap map = list.add();
            map.put("title", esc(b.getBook().getTitle().getString(lang)));
            map.put("link", "/p/" + branch + "/" + b.getBook().getFolder() + "/" + lang);
        }
    }

    private void fillSeiten(String branch, String bookFolder, SeitenSO seiten, String lang, StringBuilder gliederung,
            boolean sorted) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        for (SeiteSO seite : seiten) {
            String link = "/p/" + branch + "/" + bookFolder + "/" + lang + "/" + esc(seite.getSeite().getId());
            gliederung.append("\t<li id=\"" + seite.getId() + "\"><a href=\"" + link + "\""
                    + ">" + esc(seite.getSeite().getTitle().getString(lang)) + "</a>" + "</li>\n");
            fillSeiten(branch, bookFolder, seite.getSeiten(), lang, gliederung, true);
        }
        gliederung.append("</ul>\n");
    }
}
