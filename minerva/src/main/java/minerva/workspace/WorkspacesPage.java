package minerva.workspace;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class WorkspacesPage extends UPage {

    @Override
    protected void execute() {
        if (MinervaWebapp.factory().isCustomerVersion()) {
            throw new RuntimeException("Page in this program version not availabe");
        }

        DataList list = list("workspaces");
        for (WorkspaceSO workspace : user.getWorkspaces()) {
            DataMap map = list.add();
            map.put("name", esc(workspace.getBranch()));
            map.put("isMaster", "master".equals(workspace.getBranch()));
        }
        put("persistenceInfo", MinervaWebapp.factory().getPersistenceInfo());
        header("Workspaces");
    }
}
