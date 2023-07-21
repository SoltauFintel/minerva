package minerva.workspace;

import java.time.format.DateTimeFormatter;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.model.StateSO;
import minerva.model.StatesSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class WorkspacesPage extends UPage {

    @Override
    protected void execute() {
        DataList list = list("workspaces");
        for (WorkspaceSO workspace : user.getWorkspaces()) {
            DataMap map = list.add();
            map.put("name", esc(workspace.getBranch()));
            map.put("isMaster", "master".equals(workspace.getBranch()));
        }
        DataList list2 = list("users");
        for (StateSO state : StatesSO.getStates()) {
            DataMap map2 = list2.add();
            map2.put("login", esc(state.getUser().getLogin()));
            map2.put("lastAction", state.getUser().getLastAction().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        list2.sort((a,b) -> b.get("lastAction").toString().compareTo(a.get("lastAction").toString()));
        put("persistenceInfo", MinervaWebapp.factory().getPersistenceInfo());
        header("Workspaces");

        if (MinervaWebapp.factory().isCustomerVersion()) {
            if (user.getCurrentWorkspace() == null) {
                throw new RuntimeException("Page in this program version not availabe");
            }
            ctx.redirect("/w/" + user.getCurrentWorkspace().getBranch());
        }
    }
}
