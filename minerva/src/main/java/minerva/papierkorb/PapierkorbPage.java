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
        String pageLanguage = workspace.getUser().getPageLanguage();
        for (WeggeworfeneSeite ws : objects) {
            String title = ws.getTitle().getString(pageLanguage);
            DataMap map = list.add();
            map.put("id", esc(ws.getId()));
            map.put("date", esc(ws.getDeleteDate()));
            map.put("by", esc(ws.getDeletedBy()));
            map.put("title", esc(title));
            map.put("parentTitle", esc(ws.getParentId() == null ? "" : ws.getParentTitle().getString(pageLanguage)));
            int n = pso.countSubpages(ws);
            map.putInt("n", n);
            map.put("hasSubpages", n > 0);
            map.put("subpageslink", "/w/" + branch + "/recycle/subpages/" + ws.getId());
            map.put("poplink", "/w/" + branch + "/recycle/pop/" + ws.getId());
            map.put("deletelink", "/w/" + branch + "/recycle/delete/" + ws.getId());
            String s = n == 0 ? "" : ".subpages";
            map.put("confirmpop", esc(n("recycle.confirm.pop" + s).replace("$t", title)));
            map.put("confirmdelete", esc(n("recycle.confirm.delete" + s).replace("$t", title)));
        }
        putSize("n", objects);
        put("empty", objects.isEmpty());
    }
}
