package gitper.persistence.gitlab;

import github.soltaufintel.amalia.web.action.Action;

/**
 * This action is called by the user to start the Gitlab authentication.
 */
public class GitlabAuthAction extends Action {
    
    @Override
    protected void execute() {
        ctx.redirect(new GitlabAuthService().getAuthUrl());
    }
}
