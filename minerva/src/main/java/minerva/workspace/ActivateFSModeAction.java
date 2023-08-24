package minerva.workspace;

import org.pmw.tinylog.Logger;

import minerva.model.UserSettingsSO;

public class ActivateFSModeAction extends WAction {

    @Override
    protected void execute() {
        UserSettingsSO us = user.getUserSettings();
        if (!us.getDelayedPush().contains(branch)) {
            us.getDelayedPush().add(branch);
            us.save();
            Logger.info(user.getLogin() + " | " + branch + " | file-system mode activated");
        }
        ctx.redirect("/w/" + branch);
    }
}
