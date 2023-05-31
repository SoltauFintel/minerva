package minerva.auth;

import github.soltaufintel.amalia.auth.AuthService;
import github.soltaufintel.amalia.auth.rememberme.NoOpRememberMe;
import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.web.action.Action;

public class LogoutAction extends Action {

    @Override
    protected void execute() {
        AuthService.logout(new WebContext(ctx), new NoOpRememberMe());
        ctx.redirect("/");
    }
}
