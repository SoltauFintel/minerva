package minerva.workspace;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.user.UAction;

public class PullWorkspace extends UAction {

    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("Page only for Gitlab mode");
        }

        String branch = ctx.pathParam("branch");
        boolean force = "1".equals(ctx.queryParam("force"));
        String book = ctx.queryParam("book");
        
        user.getWorkspace(branch).pull(force);

        if (StringService.isNullOrEmpty(book)) {
            ctx.redirect("/b/" + branch);
        } else {
            ctx.redirect("/b/" + branch + "/" + book);
        }
    }
}
