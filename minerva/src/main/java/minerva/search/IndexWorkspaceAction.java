package minerva.search;

import minerva.user.UAction;

public class IndexWorkspaceAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");

        user.onlyAdmin();
        user.log("Indexing...");
        user.getWorkspace(branch).getSearch().indexBooks();
        user.log("Indexing finished.");

        ctx.redirect("/message?m=3");
    }
}
