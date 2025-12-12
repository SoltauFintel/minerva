package minerva.workspace;

public class ToggleBrokenLinkAction extends WAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        boolean showAll = "all".equals(ctx.queryParam("m"));

        user.toggleIgnoredBrokenLink(id);

        ctx.redirect("/w/" + branch + "/broken-links" + (showAll ? "?m=all" : ""));
    }
}
