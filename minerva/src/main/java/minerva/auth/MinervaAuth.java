package minerva.auth;

import github.soltaufintel.amalia.auth.AbstractAuth;
import github.soltaufintel.amalia.auth.IAuthService;
import github.soltaufintel.amalia.auth.rememberme.NoOpRememberMe;
import github.soltaufintel.amalia.auth.webcontext.WebContext;
import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.config.AppConfig;

public class MinervaAuth extends AbstractAuth {
	
	public MinervaAuth() {
		super(new NoOpRememberMe(), new MinervaAuthRoutes());
		WebContext.setCookieName(new AppConfig());
	}
	
	@Override
	public void filter(WebContext ctx) {
		super.filter(ctx);
	}
	
	@Override
	public IAuthService getService(Context ctx) {
		throw new UnsupportedOperationException();
	}
}
