package minerva.search;

import minerva.user.UAction;

public class IndexWorkspaceAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");

        user.onlyAdmin();
        user.getWorkspace(branch).getSearch().indexBooks();

        ctx.redirect("/message?m=3");
    }
}
