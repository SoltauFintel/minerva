package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Action;
import minerva.base.StringService;

public class Book6LoginAction extends Action {
    public static final String PASSWORD = "1234"; // TODO FIXME just for development; password will be changed later
    public static String _user;
    
    @Override
    protected void execute() {
        String body = ctx.req.body(); // "password=...;user=..."
        if (body == null || body.isBlank()) {
            throw new RuntimeException("No access granted.");
        }

        String[] w = body.split(";");
        String user = "";
        String password = "";
        for (String i : w) {
            String[] e = i.split("=");
            if ("user".equals(e[0])) {
                user = e[1];
            } else if ("password".equals(e[0])) {
                password = e[1];
            }
        }
        if (StringService.isNullOrEmpty(user)) {
            throw new RuntimeException("user must not be empty");
        }
        if (!PASSWORD.equals(password)) {
            Logger.error("Wrong password");
            throw new RuntimeException("No access granted.");
        }
        Logger.info("user: " + user + ", password: " + password);
        _user = user;
    }

    @Override
    protected String render() {
        return "abc"; // TODO key generieren und user merken
    }
}
