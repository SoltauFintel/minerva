package minerva.auth;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.user.UAction;

public class ActivateAdminRightsAction extends UAction {

    @Override
    protected void execute() {
        String login = user.getUser().getLogin();
        if (MinervaWebapp.factory().getAdmins().contains(login)) {
            if ("0".equals(ctx.queryParam("m"))) {
                ctx.req.session().attribute("admin", "0");
                Logger.info(login + " | dropped admin rights");
            } else {
                ctx.req.session().attribute("admin", "1");
                Logger.info(login + " | admin rights activated");
            }
        }
        ctx.redirect("/b/" + esc(user.getCurrentWorkspace().getBranch()));
    }
}
