package minerva.workspace;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.model.WorkspacesSO;
import minerva.user.UPage;

public class CreateBranchPage extends UPage {

    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("Page only for Gitlab mode");
        }
        String branch = ctx.pathParam("branch");
        if (isPOST()) {
            String newBranch = ctx.formParam("newBranch");
            if (StringService.isNullOrEmpty(newBranch)
                    || newBranch.toLowerCase().startsWith(WorkspacesSO.MINERVA_BRANCH + "-")) {
                Logger.error(user.getLogin() + " | Invalid branch name: \"" + newBranch + "\"");
                throw new UserMessage("validBranchName", user);
            }
            Logger.info(user.getLogin() + " | " + branch + " | create branch: " + newBranch);
            user.log(branch + " | create branch: " + newBranch);
            
            user.getWorkspace(branch).createBranch(newBranch, null);
            user.getWorkspaces().addWorkspace(newBranch, user);
            
            ctx.redirect("/b/" + esc(newBranch)); // show new branch
        } else {
            header(n("createBranch"));
            put("branch", esc(branch));
            put("createBranchM", esc(n("createBranchM")).replace("$m", "<b>" + esc(branch) + "</b>"));
        }
    }
}
