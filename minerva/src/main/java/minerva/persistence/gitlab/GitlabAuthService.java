package minerva.persistence.gitlab;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.User;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.base.Tosmap;
import minerva.model.GitFactory;

public class GitlabAuthService {
    private static final String STATE_PREFIX = "minerva-state-";
    private final AppConfig cfg = new AppConfig();
    
    public String getAuthUrl() {
        String appId = u(cfg.get("gitlab-appid"));
        String state = createState();
        String callback = u(cfg.get("gitlab-auth-callback"));
        String url = cfg.get("gitlab.url") + "/oauth/authorize?scope=api&client_id=" + appId
                + "&redirect_uri=" + callback + "&response_type=code&state=" + state;
        return url;
    }
    
    private String createState() {
        String state = STATE_PREFIX + IdGenerator.createId6();
        Tosmap.add(state, System.currentTimeMillis() + 1000 * 60 * 3, state);
        return state;
    }
    
    public boolean processCallback(String code, String state, DoLogin loginAction) {
        if (code == null || code.isEmpty()) {
            Logger.error("code is empty");
            return false;
        } else if (isStateValid(state)) {
            String param = getParam() + "&code=" + u(code) + "&grant_type=authorization_code";
            Answer answer = new REST(cfg.get("gitlab.url") + "/oauth/token").post(param).fromJson(Answer.class);

            try (GitLabApi gitLabApi = GitFactory.initWithAccessToken(answer.getAccess_token())) {
                initUser(gitLabApi, loginAction, answer);
                // Redirect is done by login2().
                return true;
            } catch (GitLabApiException e) {
                Logger.error(e);
                return false;
            }
        } else {
            Logger.debug("Wrong state");
            return false;
        }
    }

    private void initUser(GitLabApi gitLabApi, DoLogin loginAction, Answer answer) throws GitLabApiException {
        User currentUser = gitLabApi.getUserApi().getCurrentUser();
        String login = currentUser.getUsername();
        String mail = currentUser.getEmail();
        
        GitlabUser user = new GitlabUser(login, "");
        user.setMail(mail);
        user.setAccessToken(answer.getAccess_token());
        user.setRefreshToken(answer.getRefresh_token());
        
        loginAction.doLogin(login, user);
        
        Logger.info(login + " | Login by OAuth2 access token ok. <" + mail + ">");
    }
    
    public interface DoLogin {
        void doLogin(String login, GitlabUser user);
    }
    
    private boolean isStateValid(String state) {
        return state != null && state.startsWith(STATE_PREFIX) && state.equals(Tosmap.pop(state));
    }

    public void refreshToken(GitlabUser user) {
        String param = getParam() + "&grant_type=refresh_token&refresh_token=" + u(user.getRefreshToken());
        Answer answer = new REST(cfg.get("gitlab.url") + "/oauth/token").post(param).fromJson(Answer.class);
        
        user.setAccessToken(answer.getAccess_token());
        user.setRefreshToken(answer.getRefresh_token());
        
        Logger.info(user.getLogin() + " | Gitlab access token refreshed");
    }
    
    private String getParam() {
        return "client_id=" + u(cfg.get("gitlab-appid")) + //
                "&client_secret=" + u(cfg.get("gitlab-secret")) + //
                "&redirect_uri=" + u(cfg.get("gitlab-auth-callback"));
    }
    
    private String u(String k) {
        return Escaper.urlEncode(k, "");
    }
}
