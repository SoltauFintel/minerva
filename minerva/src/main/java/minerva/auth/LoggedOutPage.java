package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;
import minerva.MinervaWebapp;
import minerva.base.NLS;

public class LoggedOutPage extends Page {
    private String lang = "en";
    
    @Override
    protected void execute() {
        String al = MinervaAuth.browserLanguage;
        if (!"de".equals(al) && !"en".equals(al)) {
            al = "en";
        }
        lang = al;
        Logger.info(MinervaAuth.browserLanguage + " | LoggedOutPage browser language: " + lang); // XXX -> .debug
        
        put("title", n("loggedOut"));
        put("withRelogin", MinervaWebapp.factory().getConfig().isGitlab());
        put("relogin", n("relogin"));
        put("link", "/");
    }
    
    private String n(String key) {
        return NLS.get(lang, key);
    }
}
