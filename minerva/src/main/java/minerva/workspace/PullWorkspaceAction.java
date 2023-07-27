package minerva.workspace;

import minerva.base.StringService;

public class PullWorkspaceAction extends WAction {

    @Override
    protected void execute() {
        boolean force = "1".equals(ctx.queryParam("force"));
        String book = ctx.queryParam("book");
        
        workspace.pull(force);

        if (StringService.isNullOrEmpty(book)) {
            ctx.redirect("/w/" + branch);
        } else {
            ctx.redirect("/b/" + branch + "/" + book);
        }
    }
}
