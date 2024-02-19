package minerva.seite.move;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.SPage;

public class MoveSeitePage extends SPage {
    
    @Override
    protected void execute() {
        MinervaWebapp.factory().getBackendService().checkIfMoveIsAllowed(workspace);

        header(n("movePage"));
        put("pageTitle", esc(seite.getTitle()));

        boolean topLevelPage = seite.getSeite().getParentId().equals(SeiteSO.ROOT_ID);
        StringBuilder gliederung = new StringBuilder();
        gliederung.append("<ul>");
        book(book, topLevelPage, viewlink + "/move-ack?parentid=root", gliederung);
        
        fillSeiten(branch, bookFolder, book.getSeiten(), user.getGuiLanguage(), gliederung, false);
        
        for (BookSO otherBook : book.getWorkspace().getBooks()) {
            String bf = otherBook.getBook().getFolder();
            if (!bf.equals(bookFolder)) {
                book(otherBook, false, viewlink + "/move-ack?folder=" + Escaper.urlEncode(bf, ""), gliederung);
            }
        }
        gliederung.append("</ul>");
        put("gliederung", gliederung.toString());
    }
    
    private void book(BookSO book, boolean topLevelPage, String href, StringBuilder gliederung) {
        gliederung.append("<li class=\"mt1\"><i class=\"fa fa-book greenbook\"></i> ");
        if (!topLevelPage) {
            gliederung.append("<a class=\"movelink\" href=\"");
            gliederung.append(href);
            gliederung.append("\">");
        }
        gliederung.append(esc(book.getTitle()));
        if (!topLevelPage) {
            gliederung.append("</a>");
        }
        gliederung.append("</li>");
    }

    private void fillSeiten(String branch, String bookFolder, SeitenSO seiten, String lang,
            StringBuilder gliederung, boolean noLink) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        for (SeiteSO seite : seiten) {
            if (noLink) {
                gliederung.append("\t<li>");
                gliederung.append(esc(seite.getSeite().getTitle().getString(lang)));
                gliederung.append("</li>\n");
                fillSeiten(branch, bookFolder, seite.getSeiten(), lang, gliederung, true);
            } else if (seite.getId().equals(id)) {
                gliederung.append("\t<li class=\"movePageCurrent\">");
                gliederung.append(esc(seite.getSeite().getTitle().getString(lang)));
                gliederung.append("</li>\n");
                fillSeiten(branch, bookFolder, seite.getSeiten(), lang, gliederung, true);
            } else if (this.seite.getSeite().getParentId().equals(seite.getId())) {
                // current parent page
                gliederung.append("\t<li>");
                gliederung.append(esc(seite.getSeite().getTitle().getString(lang)));
                gliederung.append(" <i>(");
                gliederung.append(n("currentParentPage"));
                gliederung.append(")</i></li>\n");
                fillSeiten(branch, bookFolder, seite.getSeiten(), lang, gliederung, false);
            } else {
                String link = viewlink + "/move-ack?parentid=" + Escaper.urlEncode(seite.getId(), "");
                gliederung.append("\t<li><a class=\"movelink\" href=\"");
                gliederung.append(link);
                gliederung.append("\">");
                gliederung.append(esc(seite.getSeite().getTitle().getString(lang)));
                gliederung.append("</a></li>\n");
                fillSeiten(branch, bookFolder, seite.getSeiten(), lang, gliederung, false);
            }
        }
        gliederung.append("</ul>\n");
    }

}
