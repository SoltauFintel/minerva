package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import minerva.MinervaWebapp;
import minerva.model.StatesSO;
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
            login(login, password, loginService);
        } else {
            put("loginError", "f".equals(ctx.queryParam("m")));
            put("withPassword", loginService.withPassword());
        }
    }

    private void login(String login, String password, LoginService loginService) {
        // Kann User angemeldet werden?
        User user = loginService.login(login, password);
        if (user == null) { // Nein...
            String name = getClass().getSimpleName().replace("Page", "").toLowerCase();
            ctx.redirect("/" + name + "?m=f");
            return;
        }
        
        login2(ctx, login, user);
    }
    
    public static void login2(Context ctx, String login, User user) {
        // An Webanwendung/Session anmelden.
        WebContext wctx = new WebContext(ctx);
        wctx.session().setUserId(login);
        wctx.session().setLogin(login);
        wctx.session().setLoggedIn(true);

        // State anlegen
        StatesSO.login(ctx, user);

        // Ursprünglich angeforderte Seite aufrufen
        String path = wctx.session().getGoBackPath();
        wctx.session().setGoBackPath(null);
        if (path == null || path.isBlank() || path.equals(ctx.path())) {
            if (MinervaWebapp.factory().isCustomerVersion()) {
                ctx.redirect("/b/master");
            } else {
                ctx.redirect("/");
            }
        } else {
            Logger.info("[Login] redirect to " + path);
            ctx.redirect(path);
        }
    }
}
