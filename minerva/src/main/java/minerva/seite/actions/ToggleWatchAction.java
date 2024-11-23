package minerva.seite.actions;

import minerva.seite.SAction;

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
