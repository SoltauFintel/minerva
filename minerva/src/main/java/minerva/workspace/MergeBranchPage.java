package minerva.workspace;

import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.MinervaWebapp;
import minerva.base.UserMessage;
import minerva.model.WorkspacesSO;
import minerva.user.UPage;

public class MergeBranchPage extends UPage {

    @Override
    protected void execute() {
        MinervaWebapp.factory().gitlabOnlyPage();
        String branch = ctx.pathParam("branch");
        if (isPOST()) {
            String sourceBranch = ctx.formParam("sourceBranch");
            boolean deleteWorkspace = "on".equals(ctx.formParam("deleteWorkspace"));
            String info = "Merge branch " + sourceBranch + " into branch " + branch
                    + (deleteWorkspace ? " and delete workspace " + sourceBranch : "");
            Logger.info(info);
            user.log(info);
       
            user.dao().mergeBranch(sourceBranch, branch, user);
            if (deleteWorkspace) {
                user.getWorkspaces().remove(user.getWorkspace(sourceBranch));
            }
            user.getWorkspace(branch).pull();
            
            ctx.redirect("/w/" + branch);
        } else {
            List<String> branches = user.dao().getBranchNames(user.getWorkspace(branch));
            branches.remove(branch);
            branches.removeIf(n -> n.startsWith(WorkspacesSO.MINERVA_BRANCH) || n.contains(WorkspacesSO.MINERVA_BRANCH));
            List<String> delayedPush = user.getUserSettings().getDelayedPush();
            branches.removeIf(n -> delayedPush.contains(n));
            if (branches.isEmpty()) {
                throw new UserMessage("noBranches", user);
            }
            header(n("mergeBranch"));
            put("branch", esc(branch));
            put("mergeInfo", esc(n("mergeInfo").replace("$t", branch)));
            put("deleteWorkspace", true);
            String select = user.getLastSelectedBranch() == null ? branches.get(0) : user.getLastSelectedBranch();
            combobox("branches", branches, select, false, model);
            ColumnFormularGenerator gen = new ColumnFormularGenerator(1, 1);
            initColumnFormularGenerator(gen);
            TemplatesInitializer.fp.setContent(gen
                    .combobox("sourceBranch", n("Branch"), 4, "branches")
                    .checkbox("deleteWorkspace", n("deleteWS"), 2, false)
                    .save(n("mergen"))
                    .getHTML(model, "/merge/" + branch, "/w/" + branch));
        }
    }
    
    @Override
    protected String getPage() {
        return "formular/" + super.getPage();
    }
}
