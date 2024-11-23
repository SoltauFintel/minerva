package minerva.seite;

import java.util.ArrayList;
import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.SeiteSO;
import minerva.workspace.WPage;

public class PagesWatchedByMePage extends WPage {

    @Override
    protected void execute() {
        List<WatchEntry> entries = new ArrayList<>();
        for (String id : user.getUser().getWatchlist()) {
            boolean watchSubpages = id.endsWith("+");
            if (watchSubpages) {
                id = id.substring(0, id.length() - 1);
            }
            SeiteSO seite = workspace.findPage(id);
            if (seite != null) { // It could be that watched page is not in current branch.
                entries.add(new WatchEntry(seite, watchSubpages));
            }
        }
        entries.sort((a, b) -> a.sort().compareToIgnoreCase(b.sort()));
        
        header(n("vonMirBeobachteteSeiten"));
        DataList list = list("watched");
        for (WatchEntry we : entries) {
            DataMap map = list.add();
            map.put("id", esc(we.seite.getId()));
            map.put("bookFolder", esc(we.seite.getBook().getBook().getFolder()));
            map.put("title", esc(we.seite.getTitle()));
            map.put("hasSubpages", we.watchSubpages);
        }
    }
    
    private static class WatchEntry {
        public final SeiteSO seite;
        public final boolean watchSubpages;

        public WatchEntry(SeiteSO seite, boolean watchSubpages) {
            this.seite = seite;
            this.watchSubpages = watchSubpages;
        }
        
        public String sort() {
            return seite.getTitle() + (watchSubpages ? "2" : "1");
        }
    }
}
