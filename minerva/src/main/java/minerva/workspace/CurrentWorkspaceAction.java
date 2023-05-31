package minerva.workspace;

import minerva.user.UAction;

public class CurrentWorkspaceAction extends UAction {

    @Override
    protected void execute() {
        if (user.getCurrentWorkspace() == null) {
            ctx.redirect("/");
        } else {
            ctx.redirect("/b/" + user.getCurrentWorkspace().getBranch());
        }
    }
}
