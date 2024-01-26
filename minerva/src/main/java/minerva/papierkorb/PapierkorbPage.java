package minerva.papierkorb;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.PapierkorbSO;
import minerva.workspace.WPage;

public class PapierkorbPage extends WPage {

    @Override
    protected void execute() {
        PapierkorbSO pso = workspace.getPapierkorb();
        List<WeggeworfeneSeite> objects = pso.list();

        header(n("papierkorb"));
        DataList list = list("objects");
        for (WeggeworfeneSeite ws : objects) {
            String bookTitle = "?";
            try {
                bookTitle = workspace.getBooks().byFolder(ws.getBookFolder()).getTitle();
            } catch (Exception ignore) {
            }
            
            DataMap map = list.add();
            map.put("id", esc(ws.getId()));
            map.put("bookFolder", esc(ws.getBookFolder()));
            map.put("bookTitle", esc(bookTitle));
            map.put("date", esc(ws.getDeleteDate()));
            map.put("by", esc(ws.getDeletedBy()));
            map.put("title", esc(ws.getTitle().getString(workspace.getUser().getPageLanguage())));
            map.put("parentTitle", esc(ws.getParentId() == null ? "" : ws.getParentTitle().getString(workspace.getUser().getPageLanguage())));
            int n = pso.countSubpages(ws);
            map.putInt("n", n);
            map.put("hasSubpages", n > 0);
            map.put("subpageslink", "/w/" + branch + "/recycle/subpages/" + ws.getId());
            map.put("poplink", "/w/" + branch + "/recycle/pop/" + ws.getId());
            map.put("deletelink", "/w/" + branch + "/recycle/delete/" + ws.getId());
        }
        putSize("n", objects);
    }
}
