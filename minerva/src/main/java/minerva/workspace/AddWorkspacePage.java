package minerva.workspace;

import java.util.List;

import minerva.MinervaWebapp;
import minerva.user.UPage;

public class AddWorkspacePage extends UPage {
    
    @Override
    protected void execute() {
        MinervaWebapp.factory().gitlabOnlyPage();
        if (isPOST()) {
            user.getWorkspaces().addWorkspace(ctx.formParam("branch"), user);
            ctx.redirect("/");
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
