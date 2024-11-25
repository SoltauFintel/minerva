package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.SPage;

public class CrossBookLinksPage extends SPage {
    private int snr;
    
    @Override
    protected void execute() {
        String m = ctx.queryParam("m");
        String link = ctx.queryParam("d");
        if ("d".equals(m)) { // delete link
            render = false;
            if (seite.getSeite().getLinks().remove(link)) {
                seite.saveMeta(seite.commitMessage("delete link"));
            }
            ctx.redirect("cross-book-links");
        } else if ("a".equals(m)) { // add link
            render = false;
            if (find(link) != null) {
                seite.getSeite().getLinks().add(link);
                seite.saveMeta(seite.commitMessage("add link"));
            }
            ctx.redirect("cross-book-links");
        } else { // show links
            list();
        }
    }

    private void list() {
        header(n("crossBookLinks"));
        put("pageTitle", esc(seite.getTitle()));
        DataList list = list("links");
        for (String link : seite.getSeite().getLinks()) {
            SeiteSO s = find(link);
            if (s != null) {
                DataMap map = list.add();
                map.put("id", "s_d" + snr++);
                map.put("bookTitle", esc(s.getBook().getTitle()));
                map.put("title", esc(s.getTitle()));
                map.put("link", esc("/s/" + branch + "/" + s.getBook().getBook().getFolder() + "/" + s.getId()));
                map.put("deletelink", "cross-book-links?m=d&d=" + u(link));
            }
        }
        list.sort((a, b) -> a.get("title").toString().compareTo(b.get("title").toString()));
        put("hasLinks", !list.isEmpty());
        // Show pages from other books
        DataList list2 = list("books");
        List<BookSO> relevantBooks = getRelevantBooks();
        put("hasBooks", !relevantBooks.isEmpty());
        for (BookSO b : relevantBooks) {
            DataMap map = list2.add();
            map.put("title", esc(b.getTitle()));
            map.put("gliederung", gliederung(b)); // no esc()
        }
    }

    private SeiteSO find(String link) {
        try {
            int o = link.indexOf("/");
            String folder = link.substring(0, o);
            String id = link.substring(o + 1);
            return workspace.getBooks().byFolder(folder)._seiteById(id);
        } catch (Exception e) {
            return null;
        }
    }

    private List<BookSO> getRelevantBooks() {
        List<BookSO> ret = new ArrayList<>();
        for (BookSO b : workspace.getBooks()) {
            if (b.getBook().getFolder().equals(book.getBook().getFolder())) {
            } else if (b.isFeatureTree() || b.isInternal()) {
            } else {
                // TODO Ich wollte irgendwann noch einschränken von welchem Buch nach welchen Büchern gelinkt werden darf.
                ret.add(b);
            }
        }
        return ret;
    }
    
    // TODO Hier Klasse Gliederung nutzbar?
    private String gliederung(BookSO b) {
        StringBuilder sb = new StringBuilder();
        g(b.getSeiten(), esc(viewlink + "/cross-book-links?m=a&d="), sb);
        return sb.toString();
    }

    private void g(SeitenSO seiten, String baselink, StringBuilder sb) {
        boolean first = true;
        for (SeiteSO s : seiten) {
            if (first) {
                sb.append("<ul>");
                first = false;
            }
            boolean found = false;
            for (String link : seite.getSeite().getLinks()) {
                int o = link.indexOf("/");
                String folder = link.substring(0, o);
                String id = link.substring(o + 1);
                if (s.getId().equals(id) && s.getBook().getBook().getFolder().equals(folder)) {
                    found = true;
                    break;
                }
            }
            sb.append("<li>");
            if (!found) {
                sb.append("<a class=\"movelink\" onclick=\"document.querySelector('#s_" + snr + "').style='';\" href=\"");
                sb.append(baselink);
                sb.append(u(s.getBook().getBook().getFolder() + "/" + s.getId()));
                sb.append("\">");
            }
            sb.append(esc(s.getTitle()));
            if (!found) {
                sb.append(" <i id=\"s_" + snr + "\" class=\"fa fa-delicious fa-spin\" style=\"display: none;\"></i></a>");
            }
            snr++;
            g(s.getSeiten(), baselink, sb);
            sb.append("</li>\n");
        }
        if (!first) {
            sb.append("</ul>");
        }
    }
}
