package minerva.workspace;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.base.StringService;
import minerva.model.UserSettingsSO;

public class DeactivateFSModePage extends WPage {
    private static final String defaultCM = "many changes";
    
    @Override
    protected void execute() {
        UserSettingsSO us = user.getUserSettings();
        if (!us.getDelayedPush().contains(branch)) {
            throw new RuntimeException("Can only be called for active file-system mode.");
        }
        header(n("endFSMode"));
        put("cm", defaultCM);
        if (isPOST()) {
            String cm = ctx.formParam("cm");
            if (StringService.isNullOrEmpty(cm)) {
                cm = defaultCM;
            }
            
            save(us, new CommitMessage(cm));
        } else if ("cancel".equals(ctx.queryParam("m"))) {
            save(us, null);
        }
    }

    private void save(UserSettingsSO us, CommitMessage cm) {
        us.getDelayedPush().remove(branch);
        us.save();
        Logger.info(user.getLogin() + " | " + branch + " | file-system mode deactivated");
        if (cm == null) {
            workspace.pull(); // revert changes
            Logger.info(user.getLogin() + " | " + branch + " | changes reverted");
        } else {
            workspace.save(cm);
            Logger.info(user.getLogin() + " | " + branch + " | changes pushed: " + cm.toString());
        }
        ctx.redirect("/w/" + branch);
    }
}
