package gitper.persistence.gitlab;

import github.soltaufintel.amalia.web.action.Action;
import gitper.Gitper;

/**
 * This action is called by Gitlab after authentication.
 */
public class GitlabAuthCallbackAction extends Action {

    @Override
    protected void execute() {
        String code = ctx.queryParam("code");
        String state = ctx.queryParam("state");

        if (!new GitlabAuthService().processCallback(code, state, user -> Gitper.gitperInterface.login2(ctx, user))) {
            ctx.redirect("/");
        }
    }
}
