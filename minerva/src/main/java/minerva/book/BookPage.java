package minerva.book;

import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class BookPage extends BPage {

    @Override
    protected void execute() {
        String userLang = user.getLanguage();

        String title = book.getBook().getTitle().getString(userLang);
        put("header", esc(title));
        put("title", esc(title.toLowerCase().contains("buch") ? title : title + " (Buch)"));
        put("positionlink", booklink + "/order");
        put("sortlink", booklink + "/sort");
        put("hasPositionlink", book.getSeiten().size() > 1);
        boolean sorted = book.getBook().isSorted();
        put("isSorted", sorted);
        put("Sortierung", n(sorted ?  "alfaSorted" : "manuSorted"));

        StringBuilder gliederung = new StringBuilder();
        fillSeiten(branch, bookFolder, book.getSeiten(), userLang, gliederung, book.getBook().isSorted());
        put("gliederung", gliederung.toString());
    }

    private void fillSeiten(String branch, String bookFolder, SeitenSO seiten, String lang, StringBuilder gliederung,
            boolean sorted) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        String hasNote = " <i class=\"fa fa-comment-o has-note\" title=\"" + n("hasNote") + "\"></i>";
        for (SeiteSO seite : seiten) {
            String title = esc(seite.getSeite().getTitle().getString(lang));
            String link = "/s/" + branch + "/" + bookFolder + "/" + esc(seite.getSeite().getId());
            gliederung.append("\t<li id=\"");
            gliederung.append(seite.getId());
            gliederung.append("\"><a href=\"");
            gliederung.append(link);
            gliederung.append("\">");
            gliederung.append(title);
            gliederung.append("</a>");
            if (!seite.getSeite().getNotes().isEmpty()) {
                gliederung.append(hasNote);
            }
            gliederung.append("</li>\n");
            fillSeiten(branch, bookFolder, seite.getSeiten(), lang, gliederung, true); // recursive
        }
        gliederung.append("</ul>\n");
    }
}
