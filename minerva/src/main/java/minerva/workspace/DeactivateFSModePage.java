package minerva.workspace;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.base.StringService;

public class DeactivateFSModePage extends WPage {
    private static final String defaultCM = "many changes";
    
    @Override
    protected void execute() {
        header(n("endFSMode"));
        put("cm", defaultCM);
        if (isPOST()) {
            String cm = ctx.formParam("cm");
            if (StringService.isNullOrEmpty(cm)) {
                cm = defaultCM;
            }
            save(new CommitMessage(cm));
        } else if ("cancel".equals(ctx.queryParam("m"))) {
            save(null);
        }
    }

    private void save(CommitMessage cm) {
        user.deactivateDelayedPush(branch);
        if (cm == null) {
            workspace.pull(); // revert changes
            Logger.info(user.getLogin() + " | " + branch + " | changes reverted");
        } else {
            workspace.save(cm);
            Logger.info(user.getLogin() + " | " + branch + " | changes pushed: " + cm.toString());
        }
        ctx.redirect("/w/" + branch + "/menu");
    }
}
