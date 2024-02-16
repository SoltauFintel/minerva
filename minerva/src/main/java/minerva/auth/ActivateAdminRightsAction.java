package minerva.auth;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.user.UAction;

/**
 * User (de)activates ability to use administrator-only commands. Only users with admin right can use this feature.
 * Users with not activated admin mode behave like normal users, except this ability.
 */
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
        ctx.redirectToReferer();
    }
}
