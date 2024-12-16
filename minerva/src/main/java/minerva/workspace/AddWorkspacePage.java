package minerva.workspace;

import java.util.List;

import minerva.MinervaWebapp;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class AddWorkspacePage extends UPage {
    
    @Override
    protected void execute() {
        MinervaWebapp.factory().gitlabOnlyPage();
        if (isPOST()) {
            String branch = ctx.formParam("branch");
            WorkspaceSO newWorkspace = user.getWorkspaces().addWorkspace(branch, user);
            String path;
            try {
                path = "/b/" + esc(newWorkspace.getBranch()) + "/" + esc(newWorkspace.getBooks().get(0).getBook().getFolder());
            } catch (Exception e) {
                path = "/";
            }
            ctx.redirect(path);
        } else {
            List<String> branchs = user.getWorkspaces().getAddableBranches(user.masterWorkspace());
            if (branchs.isEmpty()) {
                render = false;
                ctx.redirect("/message?m=2"); // noBranchToAdd
            } else {
                header(n("createWS"));
                combobox("branchs", branchs, "", false);
            }
        }
    }
}
