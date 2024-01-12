package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;
import minerva.MinervaWebapp;
import minerva.base.NLS;

public class LoggedOutPage extends Page {
    private String lang = "en";
    
    @Override
    protected void execute() {
        String al = ctx.req.headers("Accept-Language");
        Logger.debug("LoggedOutPage Accept-Language: " + al);
        if (al.startsWith("de")) {
            lang = "de";
        } else {
            lang = "en";
        }
        
        put("title", n("loggedOut"));
        put("withRelogin", MinervaWebapp.factory().getConfig().isGitlab());
        put("relogin", n("relogin"));
        put("link", "/");
    }
    
    private String n(String key) {
        return NLS.get(lang, key);
    }
}
