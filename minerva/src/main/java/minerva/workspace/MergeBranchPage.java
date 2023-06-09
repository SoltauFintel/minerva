package minerva.workspace;

import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.MinervaWebapp;
import minerva.base.UserMessage;
import minerva.model.WorkspacesSO;
import minerva.persistence.gitlab.GitlabUser;
import minerva.user.UPage;

public class MergeBranchPage extends UPage {

    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("Page only for Gitlab mode");
        }
        String branch = ctx.pathParam("branch");
        if (isPOST()) {
            String sourceBranch = ctx.formParam("sourceBranch");
            Logger.info("Merge branch " + sourceBranch + " into branch " + branch);
            user.log("Merge branch " + sourceBranch + " into branch " + branch);
       
            user.dao().mergeBranch(sourceBranch, branch, (GitlabUser) user.getUser());
            user.getWorkspace(branch).pull();
            
            ctx.redirect("/b/" + branch);
        } else {
            List<String> branches = user.dao().getBranchNames(user.getWorkspace(branch));
            branches.remove(branch);
            branches.removeIf(n -> n.startsWith(WorkspacesSO.MINERVA_BRANCH) || n.contains(WorkspacesSO.MINERVA_BRANCH));
            if (branches.isEmpty()) {
                throw new UserMessage("noBranches", user);
            }
            header(n("mergeBranch"));
            put("branch", esc(branch));
            put("mergeInfo", esc(n("mergeInfo").replace("$t", branch)));
            combobox("branches", branches, branches.get(0), false, model);
            ColumnFormularGenerator gen = new ColumnFormularGenerator(1, 1);
            initColumnFormularGenerator(gen);
            TemplatesInitializer.fp.setContent(gen
                    .combobox("sourceBranch", n("Branch"), 4, "branches")
                    .save(n("mergen"))
                    .getHTML("/merge/" + branch, "/b/" + branch));
        }
    }
    
    @Override
    protected String getPage() {
        return "formular/" + super.getPage();
    }
}
