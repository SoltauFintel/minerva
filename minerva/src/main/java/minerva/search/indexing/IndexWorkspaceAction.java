package minerva.search.indexing;

import minerva.user.UAction;

public class IndexWorkspaceAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        
        user.getWorkspace(branch).getSearch().indexBooks();

        ctx.redirect("/message?m=3");
    }
}
