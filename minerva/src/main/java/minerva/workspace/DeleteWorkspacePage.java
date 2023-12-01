package minerva.workspace;

import minerva.base.UserMessage;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class DeleteWorkspacePage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");

        if ("master".equals(branch)) {
            throw new UserMessage(n("deleteWS3"), user); // master can't be deleted
        } else if (user.getWorkspaces().size() == 1) {
            throw new UserMessage(n("deleteWS4"), user); // last can't be deleted
        }
        
        WorkspaceSO workspace = user.getWorkspace(branch);

        put("branch", esc(branch));
        header(n("deleteWS"));
        
        if ("d".equals(ctx.queryParam("m"))) {
            user.getWorkspaces().remove(workspace);
            ctx.redirect("/w/master/menu");
        }
    }
}
