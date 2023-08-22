package minerva.auth;

import java.time.LocalDateTime;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.auth.AbstractAuth;
import github.soltaufintel.amalia.auth.AuthService;
import github.soltaufintel.amalia.auth.IAuthService;
import github.soltaufintel.amalia.auth.rememberme.NoOpRememberMe;
import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
import minerva.model.StateSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.persistence.gitlab.GitFactory;
import minerva.user.User;

public class MinervaAuth extends AbstractAuth {
    
    public MinervaAuth() {
        super(new NoOpRememberMe(), new MinervaAuthRoutes());
    }
    
    @Override
    public void filter(WebContext ctx) {
        super.filter(ctx);

        StateSO stateSO = StatesSO.get(ctx.req().session().id());
        if (stateSO != null) {
            UserSO user = stateSO.getUser();
            if (user != null) {
                user.setLastAction(LocalDateTime.now());
            }
        }
    }
    
    @Override
    public IAuthService getService(Context ctx) {
        throw new UnsupportedOperationException();
    }

    public static WebContext login1(Context ctx, User user) {
        // An Webanwendung/Session anmelden.
        WebContext wctx = new WebContext(ctx);
        wctx.session().setUserId(user.getLogin());
        wctx.session().setLogin(user.getLogin());
        wctx.session().setLoggedIn(true);
    
        // State anlegen
        StatesSO.login(ctx, user);
        return wctx;
    }

    public static void login2(Context ctx, User user) {
        WebContext wctx = login1(ctx, user);

        // Ursprünglich angeforderte Seite aufrufen
        String path = wctx.session().getGoBackPath();
        wctx.session().setGoBackPath(null);
        if (path == null || path.isBlank() || path.equals(ctx.path())) {
            if (MinervaWebapp.factory().isCustomerVersion()) {
                ctx.redirect("/w/master");
            } else {
                ctx.redirect("/");
            }
        } else {
            if ("/b/master".equals(path)) {
                path = "/w/master";
            }
            Logger.info(user.getLogin() + " | Redirect to " + path + " after login");
            ctx.redirect(path);
        }
    }
    
    public static void logout(Context ctx) {
        StateSO stateSO = StatesSO.get(ctx);
        if (stateSO != null) {
            stateSO.getUser().finishMyEditings();
            boolean revoked = GitFactory.logout(stateSO.getUser().getUser());
            Logger.info(stateSO.getUser().getLogin() + " | logout" + (revoked ? " | Gitlab revoke ok" : ""));
        }
        logout2(ctx);
    }
    
    public static void logout2(Context ctx) {
        AuthService.logout(new WebContext(ctx), new NoOpRememberMe());
        ctx.req.session().invalidate();
    }
}
