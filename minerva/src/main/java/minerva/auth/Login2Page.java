package minerva.auth;

import org.pmw.tinylog.Logger;

/**
 * Backdoor: login page with user and password
 */
public class Login2Page extends LoginPage {

    @Override
    protected void execute() {
        Logger.info("backdoor");
        super.execute();
    }
    
    @Override
    protected String errorUrl() {
        return "/backdoor?m=f";
    }
}
