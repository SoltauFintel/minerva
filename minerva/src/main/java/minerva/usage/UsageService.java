package minerva.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

public class UsageService {

    /**
     * User Story: Nutzungsdaten von Online-Hilfe laden
     * @param host z.B. http://4580.doc.x-map.de
     * @param from Format JJJJ-MM-TT'T'HHmmss
     * @param to Format JJJJ-MM-TT'T'HHmmss, darf null sein
     * @param workspace -
     * @return Usage list, nie null. Exception möglich.
     */
    public List<Usage> loadUsage(String host, String from, String to, WorkspaceSO workspace) {
        var usages = new REST(host + "/usage?from=" + u(from) + (to == null ? "" : "&to=" + u(to))).get()
                .fromJson(Usages.class);
        List<Usage> ret = usages == null || usages.getUsages() == null ? new ArrayList<>() : usages.getUsages();
        for (Usage u : ret) {
            SeiteSO seite = workspace.findPage(u.getPageId()); // TODO evtl. zu teuer / und Seiten kommen ja mehrfach vor
            if (seite != null) {
                u.setTitle(seite.getTitle());
                u.setLink(seite.viewlink());
            } else {
                u.setTitle("#" + u.getPageId()); // unknown page
                // Es kann ein Buch Folder sein.
                for (BookSO book : workspace.getBooks()) {
                    if (book.getBook().getFolder().equals(u.getPageId())) {
                        u.setTitle(book.getTitle());
                        u.setLink("/b/" + workspace.getBranch() + "/" + book.getBook().getFolder());
                    }
                }
            }
        }
        return ret;
    }
    
    private static String u(String x) {
        return Escaper.urlEncode(x, x);
    }
    
    public TreeSet<String> getCustomers() {
        TreeSet<String> ret = new TreeSet<>();
        String c = new AppConfig().get("usage.customers", "");
        for (String i : c.split(",")) {
            if (!i.isBlank()) {
                ret.add(i.trim());
            }
        }
        return ret;
    }
}
