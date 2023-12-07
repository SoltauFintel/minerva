package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;
import minerva.base.NLS;

public class LoggedOutPage extends Page {
    private String lang;
    
    @Override
    protected void execute() {
        String lang = ctx.req.headers("Accept-Language");
        Logger.debug("LoggedOutPage Accept-Language: " + lang);
        if (lang.startsWith("de")) {
            lang = "de";
        } else {
            lang = "en";
        }
        
        put("title", n("loggedOut"));
        put("relogin", n("relogin"));
        put("link", "/");
    }
    
    private String n(String key) {
        return NLS.get(lang, key);
    }
}
