package minerva.seite;

public class ToggleWatchAction extends SAction {

    @Override
    protected void execute() {
        if ("s".equals(ctx.queryParam("m"))) {
            user.toggleWatch(id + "+");
        } else {
            user.toggleWatch(id);
        }
        
        ctx.redirect(viewlink);
    }
}
