package minerva.auth;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.user.UAction;

public class ActivateAdminRightsAction extends UAction {

    @Override
    protected void execute() {
        String login = user.getLogin();
        if (MinervaWebapp.factory().getAdmins().contains(login)) {
            if ("0".equals(ctx.queryParam("m"))) {
                ctx.req.session().attribute("admin", "0");
                Logger.info(login + " | dropped admin rights");
                user.log("-- Admin rights dropped.");
            } else {
                ctx.req.session().attribute("admin", "1");
                Logger.info(login + " | admin rights activated");
                user.log("-- Admin rights activated.");
            }
        }
        ctx.redirect("/w/" + esc(user.getCurrentWorkspace().getBranch()));
    }
}
