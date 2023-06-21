package minerva.persistence.gitlab;

import java.util.HashSet;
import java.util.Set;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.action.Action;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.config.AppConfig;

/**
 * This action is called by the user to start the Gitlab authentication.
 */
// TODO refresh token Sache...
public class GitlabAuthAction extends Action {
    public static final Set<String> knownStates = new HashSet<>(); // TODO nicht die beste Technik das so zu machen,
    // aber mit session hatte es nicht auf Anhieb geklappt. TODO Set overflow
    // TODO move code to service class
    
    @Override
    protected void execute() {
        AppConfig cfg = new AppConfig();
        String appId = u(cfg.get("gitlab-appid"));
        String state = IdGenerator.createId6();
        knownStates.add(state);
        String callback = u(cfg.get("gitlab-auth-callback"));
        String url = cfg.get("gitlab.url") + "/oauth/authorize?scope=api&client_id=" + appId
                + "&redirect_uri=" + callback + "&response_type=code&state=" + state;
        ctx.redirect(url);
    }
    
    private String u(String k) {
        return Escaper.urlEncode(k, "");
    }
}
