package minerva.persistence.gitlab;

import github.soltaufintel.amalia.web.action.Action;
import minerva.auth.LoginPage;

/**
 * This action is called by Gitlab after authentication.
 */
public class GitlabAuthCallbackAction extends Action {

    @Override
    protected void execute() {
        String code = ctx.queryParam("code");
        String state = ctx.queryParam("state");

        if (!new GitlabAuthService().processCallback(code, state, (login, user) -> LoginPage.login2(ctx, login, user))) {
            ctx.redirect("/");
        }
    }
}
