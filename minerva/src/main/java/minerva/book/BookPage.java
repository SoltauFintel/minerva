package minerva.book;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.ViewSeitePage;

public class BookPage extends BPage {

    @Override
    protected void execute() {
        boolean allPages = "all".equals(ctx.queryParam("m"));
        String userLang = user.getGuiLanguage();

        String title = book.getBook().getTitle().getString(userLang);
        put("header", esc(title));
        put("title", esc(title.toLowerCase().contains("buch") ? title : title + " (Buch)"));
        put("positionlink", booklink + "/order");
        put("sortlink", booklink + "/sort");
        put("hasPositionlink", book.getSeiten().size() > 1);
        boolean sorted = book.getBook().isSorted();
        put("isSorted", sorted);
        put("Sortierung", n(sorted ?  "alfaSorted" : "manuSorted"));
        put("hasPrevlink", false);
        put("hasNextlink", false);
        SeiteSO change = book.getLastChange();
        put("hasLastChange", change != null);
        if (change != null) {
            ViewSeitePage.fillLastChange(change.getLastChange(), change.getTitle(), n("lastChangeInfoForBook"), model);
        }

        DataList list = list("languages");
        for (String lang : langs) {
            DataMap map = list.add();
            StringBuilder gliederung = new StringBuilder();
            fillSeiten(branch, bookFolder, book.getSeiten(), lang, allPages, book.getBook().isSorted(), gliederung);
            map.put("lang", lang);
            map.put("LANG", lang.toUpperCase());
            map.put("gliederung", gliederung.toString());
            map.put("active", user.getPageLanguage().equals(lang));
            map.put("bookTitle", esc(book.getBook().getTitle().getString(lang)));
        }
    }

    private void fillSeiten(String branch, String bookFolder, SeitenSO seiten, String lang, boolean allPages,
            boolean sorted, StringBuilder gliederung) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        String hasNote = " <i class=\"fa fa-comment-o has-note\" title=\"" + n("hasNote") + "\"></i>";
        for (SeiteSO seite : seiten) {
            int hasContent = seite.hasContent(lang);
            if (hasContent > 0 || allPages) {
                String title = esc(seite.getSeite().getTitle().getString(lang));
                String link = "/s/" + branch + "/" + bookFolder + "/" + esc(seite.getSeite().getId());
                String nc = hasContent == 2 ? " class=\"noContent\"" : "";
                if (allPages && hasContent == 0) {
                    nc = " class=\"hiddenPage\"";
                }
                gliederung.append("\t<li id=\"");
                gliederung.append(seite.getId());
                gliederung.append("\"><a href=\"");
                gliederung.append(link);
                gliederung.append("\"" + nc + ">");
                gliederung.append(title);
                gliederung.append("</a>");
                if (!seite.getSeite().getNotes().isEmpty()) {
                    gliederung.append(hasNote);
                }
                gliederung.append("</li>\n");
                
                fillSeiten(branch, bookFolder, seite.getSeiten(), lang, allPages, true, gliederung); // recursive
            }
        }
        gliederung.append("</ul>\n");
    }
}
