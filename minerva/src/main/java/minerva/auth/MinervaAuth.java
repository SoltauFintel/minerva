package minerva.auth;

import java.time.LocalDateTime;

import github.soltaufintel.amalia.auth.AbstractAuth;
import github.soltaufintel.amalia.auth.IAuthService;
import github.soltaufintel.amalia.auth.rememberme.NoOpRememberMe;
import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.spark.Context;
import minerva.model.StateSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;

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
}
