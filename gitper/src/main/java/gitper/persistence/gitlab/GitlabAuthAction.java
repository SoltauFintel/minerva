package gitper.persistence.gitlab;

import github.soltaufintel.amalia.web.action.Action;
import gitper.Gitper;

/**
 * This action is called by the user to start the Gitlab authentication.
 */
public class GitlabAuthAction extends Action {
    
    @Override
    protected void execute() {
        ctx.redirect(Gitper.gitperInterface.authService().getAuthUrl());
    }
}
