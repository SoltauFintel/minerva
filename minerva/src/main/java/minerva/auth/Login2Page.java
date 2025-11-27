package minerva.auth;

import org.pmw.tinylog.Logger;

import gitper.BackendService;
import gitper.Gitper;
import minerva.MinervaWebapp;

/**
 * Backdoor: login page with user and password
 */
public class Login2Page extends LoginPage {

    @Override
    protected void execute() {
        Logger.info("backdoor");
        if (isPOST()) {
            BackendService loginService = MinervaWebapp.factory().getBackendService();
            String login = ctx.formParam("user[login]"); // gleiche name's wie bei Gitlab
            String password = ctx.formParam("user[password]");
            Logger.info("LoginPage POST " + login);

            // Kann User angemeldet werden?
            gitper.User user = loginService.login(login, password, null);
            if (user == null) { // Nein...
                ctx.redirect("/backdoor?m=f");
                return;
            }
            
            Gitper.gitperInterface.login2(ctx, user);
        } else {
            super.execute();
            put("loginError", "f".equals(ctx.queryParam("m")));
        }
    }
}
