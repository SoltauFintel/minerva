package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;
import gitper.BackendService;
import minerva.MinervaWebapp;

/**
 * File-system (customer version): "Please select Help > Edit online help in the application." page
 * 
 * <p>Gitlab: redirect to Gitlab login
 */
public class LoginPage extends Page {

    @Override
    protected void execute() {
        BackendService loginService = MinervaWebapp.factory().getBackendService();
        boolean withPassword = loginService.withPassword();
        Logger.debug("LoginPage, " + loginService.getClass().getSimpleName() +
        		", " + (withPassword ? "login with Gitlab" : "login with login/password"));
        put("withPassword", withPassword);
        if (withPassword) {
            ctx.redirect("/gitlab-auth");
        }
    }
}
