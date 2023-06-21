package minerva.auth;

import org.pmw.tinylog.Logger;

/**
 * Old login page with user and password
 */
public class Login2Page extends LoginPage {

    @Override
    protected void execute() {
        Logger.info("old login page");
        super.execute();
    }
}
