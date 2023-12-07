package minerva.auth;

import github.soltaufintel.amalia.web.action.Action;
import minerva.model.SessionExpiredException;

public class LogoutAction extends Action {

    @Override
    protected void execute() {
        try {
            MinervaAuth.logout(ctx);
        } catch (SessionExpiredException ignore) {
        }
        
        ctx.redirect("/logged-out");
    }
}
