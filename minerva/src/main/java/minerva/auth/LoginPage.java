package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;
import minerva.MinervaWebapp;
import minerva.user.User;

public class LoginPage extends Page {
    // TO-DO Es muss später für F1 noch einen POST-basierten Login geben, mit Passwort.

    @Override
    protected void execute() {
        LoginService loginService = MinervaWebapp.factory().getLoginService();
        if (isPOST()) {
            String login = ctx.formParam("user[login]"); // gleiche name's wie bei Gitlab
            String password = loginService.withPassword() ? ctx.formParam("user[password]") : null;
            Logger.info("LoginPage POST " + login);

            // Kann User angemeldet werden?
            User user = loginService.login(login, password);
            if (user == null) { // Nein...
                String name = getClass().getSimpleName().replace("Page", "").toLowerCase();
                ctx.redirect("/" + name + "?m=f");
                return;
            }
            
            MinervaAuth.login2(ctx, user);
        } else {
            Logger.debug("LoginPage " + loginService.getClass().getSimpleName());
            put("loginError", "f".equals(ctx.queryParam("m")));
            put("withPassword", loginService.withPassword());
        }
    }
}
