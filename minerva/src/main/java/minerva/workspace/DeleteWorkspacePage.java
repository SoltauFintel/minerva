package minerva.workspace;

import minerva.base.UserMessage;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class DeleteWorkspacePage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");

        if ("master".equals(branch)) {
            throw new UserMessage("Workspace \"master\" is not allowed to be deleted!", user);
        } else if (user.getWorkspaces().size() == 1) {
            throw new UserMessage("Last workspace can not be deleted!", user);
        }
        
        WorkspaceSO workspace = user.getWorkspace(branch);

        put("branch", branch);
        header(n("deleteWS"));
        
        if ("d".equals(ctx.queryParam("m"))) {
            user.getWorkspaces().remove(workspace);
            ctx.redirect("/");
        }
    }
}
