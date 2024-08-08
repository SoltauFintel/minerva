package minerva.base;

import minerva.user.UPage;

/**
 * Display important operations to administrator
 */
public class ServerlogPage extends UPage {

    @Override
    protected void execute() {
        user.onlyAdmin();
        user.log("-- Server log called.");
        
        header(n("serverlog"));
        put("serverlog", esc(user.getServerlog()));
    }
}
