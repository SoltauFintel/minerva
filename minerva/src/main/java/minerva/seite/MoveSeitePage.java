package minerva.seite;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class MoveSeitePage extends SPage {

    @Override
    protected void execute() {
        header("Seite verschieben");
        put("pageTitle", esc(seite.getTitle()));

        StringBuilder gliederung = new StringBuilder();
        gliederung.append("<ul><li><a href=\"/s/"+branch+"/"+bookFolder+ "/"+esc(id)+"/moved?parentid=root\">"
                + esc(book.getTitle()) + "</a></li>");
        fillSeiten(branch, bookFolder, book.getSeiten(), user.getLanguage(), gliederung);
        gliederung.append("</ul>");
        put("gliederung", gliederung.toString());
    }

    private void fillSeiten(String branch, String bookFolder, SeitenSO seiten, String lang, StringBuilder gliederung) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        for (SeiteSO seite : seiten) {
            if (!seite.getId().equals(id)) {
                if (this.seite.getSeite().getParentId().equals(seite.getId())) {
                    // current parent page
                    gliederung.append("\t<li>" + esc(seite.getSeite().getTitle().getString(lang)) + "</li>\n");
                } else {
                    String link = viewlink + "/move-ack?parentid=" + Escaper.urlEncode(seite.getId(), "");
                    gliederung.append("\t<li><a href=\"" + link + "\">" + esc(seite.getSeite().getTitle().getString(lang))
                            + "</a></li>\n");
                }
                fillSeiten(branch, bookFolder, seite.getSeiten(), lang, gliederung);
            }
        }
        gliederung.append("</ul>\n");
    }

}
