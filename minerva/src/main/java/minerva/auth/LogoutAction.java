package minerva.auth;

import github.soltaufintel.amalia.web.action.Action;

public class LogoutAction extends Action {

    @Override
    protected void execute() {
        MinervaAuth.logout(ctx);
        
        ctx.redirect("/");
    }
}
