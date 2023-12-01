package minerva.workspace;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
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
            map.put("link", workspace.getBooks().isEmpty() ? "" :
            	("/b/" + esc(workspace.getBranch()) + "/" + esc(workspace.getBooks().get(0).getBook().getFolder())));
        }
        put("persistenceInfo", MinervaWebapp.factory().getBackendService().getInfo());
        header("Workspaces");

        if (MinervaWebapp.factory().isCustomerVersion()) {
            if (user.getCurrentWorkspace() == null) {
                throw new RuntimeException("Page in this program version not availabe");
            }
            ctx.redirect("/w/" + user.getCurrentWorkspace().getBranch());
        }
    }
}
