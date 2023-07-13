package minerva.exclusions;

import org.pmw.tinylog.Logger;

import minerva.model.ExclusionsSO;
import minerva.persistence.gitlab.UpToDateCheckService;
import minerva.user.UPage;

public class ExclusionsEditPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        
        user.onlyAdmin();
        
        if (isPOST()) {
            String exclusions = ctx.formParam("exclusions");
            ex(branch).set(exclusions);
            Logger.info(user.getUser().getLogin() + " | " + branch + " | Exclusions saved.");
            ctx.redirect("/w/" + esc(branch) + "/exclusions/edit");
        } else {
            UpToDateCheckService.check(user.getWorkspace(branch), () -> {});
            header(n("exclusions"));
            put("exclusions", esc(ex(branch).get()));
        }
    }

    private ExclusionsSO ex(String branch) {
        return user.getWorkspace(branch).getExclusions();
    }
}
