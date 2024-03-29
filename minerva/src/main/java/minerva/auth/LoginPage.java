package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;
import minerva.MinervaWebapp;
import minerva.config.BackendService;
import minerva.user.User;

// TODO Brauch ich das hier eigentlich?
public class LoginPage extends Page {

    @Override
    protected void execute() {
        BackendService loginService = MinervaWebapp.factory().getBackendService();
        if (isPOST()) {
            String login = ctx.formParam("user[login]"); // gleiche name's wie bei Gitlab
            String password = ctx.formParam("user[password]");
            Logger.info("LoginPage POST " + login);

            // Kann User angemeldet werden?
            User user = loginService.login(login, password, null);
            if (user == null) { // Nein...
                ctx.redirect(errorUrl());
                return;
            }
            
            MinervaAuth.login2(ctx, user);
        } else {
            Logger.debug("LoginPage " + loginService.getClass().getSimpleName());
            put("loginError", "f".equals(ctx.queryParam("m")));
            boolean withPassword = loginService.withPassword();
            put("withPassword", withPassword);
            if (withPassword) {
                ctx.redirect("/gitlab-auth");
            }
        }
    }
    
    protected String errorUrl() { // TODO Wer ruft das hier auf?
        return "/login?m=f";
    }
}
