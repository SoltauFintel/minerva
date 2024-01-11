package minerva.subscription;

import github.soltaufintel.amalia.web.action.Action;

public class PushDataAction extends Action {

    @Override
    protected void execute() {
        new SubscriptionService().pagesChanged();
        ctx.redirect("/w/" + esc(ctx.pathParam("branch")) + "/menu");
    }
}
