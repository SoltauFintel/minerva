package minerva.auth;

import github.soltaufintel.amalia.auth.AuthService;
import github.soltaufintel.amalia.auth.rememberme.NoOpRememberMe;
import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.web.action.Action;
import minerva.model.GitFactory;
import minerva.model.StatesSO;

public class LogoutAction extends Action {

    @Override
    protected void execute() {
        GitFactory.logout(StatesSO.get(ctx).getUser().getUser());
        AuthService.logout(new WebContext(ctx), new NoOpRememberMe());
        ctx.req.session().invalidate();
        ctx.redirect("/");
    }
}
