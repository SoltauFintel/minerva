package minerva.workspace;

import java.util.List;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.MinervaWebapp;
import minerva.user.UPage;

public class AddWorkspacePage extends UPage {

    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("Page only for Gitlab mode");
        }
        if (isPOST()) {
            String branch1 = ctx.queryParam("branch");
            
            user.getWorkspaces().addWorkspace(branch1, user);

            ctx.redirect("/");
        } else {
            List<String> branches = user.getWorkspaces().getAddableBranches(user.getWorkspaces().master());
            
            combobox("branches", branches, null, false, model);
            header(n("createWS"));
            
            ColumnFormularGenerator gen = new ColumnFormularGenerator(1, 1);
            initColumnFormularGenerator(gen);
            TemplatesInitializer.fp.setContent(gen
                    .combobox("branch", "Branch", 4, "branches", true)
                    .getHTML("/create-workspace", "/"));
        }
    }
    
    @Override
    protected String getPage() {
        return "formular/" + super.getPage();
    }
}
