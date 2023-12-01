package minerva.workspace;

public class PullWorkspaceAction extends WAction {

    @Override
    protected void execute() {
        boolean force = "1".equals(ctx.queryParam("force"));
        
        workspace.pull(force);

        ctx.redirect(ctx.req.headers("Referer"));
    }
}
