package minerva.auth;

import github.soltaufintel.amalia.auth.AuthService;
import github.soltaufintel.amalia.auth.rememberme.NoOpRememberMe;
import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.web.action.Action;
import minerva.model.GitFactory;
import minerva.model.StateSO;
import minerva.model.StatesSO;

public class LogoutAction extends Action {

    @Override
    protected void execute() {
        StateSO stateSO = StatesSO.get(ctx);
        if (stateSO != null) {
            GitFactory.logout(stateSO.getUser().getUser());
        }
        AuthService.logout(new WebContext(ctx), new NoOpRememberMe());
        ctx.req.session().invalidate();
        ctx.redirect("/");
    }
}
