package minerva.auth;

import github.soltaufintel.amalia.auth.IAuthRoutes;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import github.soltaufintel.amalia.web.route.RouteHandler;

public class MinervaAuthRoutes extends RouteDefinitions implements IAuthRoutes {

    public MinervaAuthRoutes() {
        super(60);
    }
    
    @Override
    public void routes() {
        setupAuthFilter();
        get("/auth/logout", LogoutAction.class);
        addNotProtected("/auth/logout");
    }

    @Override
    public RouteHandler getLoginPageRouteHandler() {
        return getRouteHandler(LoginPage.class); // TODO Das ist so nicht gewollt.
    }
}
