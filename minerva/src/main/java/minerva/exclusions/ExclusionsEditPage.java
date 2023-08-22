package minerva.exclusions;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.model.ExclusionsSO;
import minerva.user.UPage;

public class ExclusionsEditPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        
        user.onlyAdmin();
        
        if (isPOST()) {
            String exclusions = ctx.formParam("exclusions");
            ex(branch).set(exclusions);
            Logger.info(user.getLogin() + " | " + branch + " | Exclusions saved.");
            user.log("Exclusions saved");
            ctx.redirect("/w/" + esc(branch) + "/exclusions/edit");
        } else {
            MinervaWebapp.factory().getBackendService().uptodatecheck(user.getWorkspace(branch), () -> {});
            header(n("exclusions"));
            put("exclusions", esc(ex(branch).get()));
        }
    }

    private ExclusionsSO ex(String branch) {
        return user.getWorkspace(branch).getExclusions();
    }
}
