package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.action.Action;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.Tosmap;

public class Book6LoginAction extends Action {
    private String id;
    
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
        if (!MinervaWebapp.factory().getConfig().getEditorPassword().equals(password)) {
            Logger.error("Wrong password");
            throw new RuntimeException("No access granted.");
        }
        Logger.info("user: " + user + ", password: " + password);
        id = "b6L-" + IdGenerator.genId();
        Tosmap.add(id, System.currentTimeMillis() + 1000 * 60, user);
    }

    @Override
    protected String render() {
        return id;
    }
}
