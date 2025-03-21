package gitper.persistence.gitlab;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import com.google.common.base.Objects;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.rest.RestStatusException;
import github.soltaufintel.amalia.web.action.Escaper;
import gitper.GitlabConfig;
import gitper.Gitper;

public class GitlabAuthService {
    private static final String STATE_PREFIX = "minerva-state-";
    private final GitlabConfig gitlabConfig;
    
    public GitlabAuthService(GitlabConfig gitlabConfig) {
    	this.gitlabConfig = gitlabConfig;
    }
    
    public String getAuthUrl() {
        String appId = u(cfg().getGitlabAppId());
        String state = createState();
        String callback = u(cfg().getGitlabAuthCallback());
        String url = cfg().getGitlabUrl() + "/oauth/authorize?scope=api&client_id=" + appId
                + "&redirect_uri=" + callback + "&response_type=code&state=" + state;
        return url;
    }
    
    private String createState() {
        String state = STATE_PREFIX + IdGenerator.createId6();
        Gitper.gitperInterface.tosmap_add(state, System.currentTimeMillis() + 1000 * 60 * 3); //Tosmap.add(state, System.currentTimeMillis() + 1000 * 60 * 3, state);
        return state;
    }
    
    public boolean processCallback(String code, String state, DoLogin loginAction) {
        if (code == null || code.isEmpty()) {
            Logger.error("code is empty");
            return false;
        } else if (isStateValid(state)) {
            String param = getParam() + "&code=" + u(code) + "&grant_type=authorization_code";
            Answer answer = new REST(cfg().getGitlabUrl() + "/oauth/token").post(param).fromJson(Answer.class);

            try (GitLabApi gitLabApi = (GitLabApi) Gitper.gitperInterface.initWithAccessToken(answer.getAccess_token())) {
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
        org.gitlab4j.api.models.User currentUser = gitLabApi.getUserApi().getCurrentUser();
        String login = currentUser.getUsername();
        String mail = currentUser.getEmail();
        
        gitper.User user = Gitper.gitperInterface.loadUser(login, true, mail);
        if (!Objects.equal(user.getMailAddress(), mail)) {
            Logger.warn("Gitlab user " + login + " mail mismatch: " + user.getMailAddress() + " <> " + mail +
                    ". Please enter mail address in admin dialog 'Manage users'.");
        }
        GitlabDataStore xu = new GitlabDataStore(user);
        xu.setPassword("");
        xu.setAccessToken(answer.getAccess_token());
        xu.setRefreshToken(answer.getRefresh_token());
        
        loginAction.doLogin(user);
        
        Logger.info(login + " | Login by OAuth2 access token ok. <" + mail + ">");
    }
    
    public interface DoLogin {
        void doLogin(gitper.User user);
    }
    
    private boolean isStateValid(String state) {
        return state != null && state.startsWith(STATE_PREFIX) && state.equals(Gitper.gitperInterface.tosmap_pop(state));
    }

    public void refreshToken(gitper.User user) {
        GitlabDataStore xu = new GitlabDataStore(user);
        String param = getParam() + "&grant_type=refresh_token&refresh_token=" + u(xu.getRefreshToken());
        Answer answer;
        try {
            answer = new REST(cfg().getGitlabUrl() + "/oauth/token").post(param).fromJson(Answer.class);
        } catch (RestStatusException e) { // Status is 401
            throw new RuntimeException(e.getMessage() + "\nTry to log out and log in.", e);
        }
        
        xu.setAccessToken(answer.getAccess_token());
        xu.setRefreshToken(answer.getRefresh_token());
        
        Logger.info(user.getLogin() + " | Gitlab access token refreshed");
    }
    
    private String getParam() {
        return "client_id=" + u(cfg().getGitlabAppId()) + //
                "&client_secret=" + u(cfg().getGitlabSecret()) + //
                "&redirect_uri=" + u(cfg().getGitlabAuthCallback());
    }
    
    private GitlabConfig cfg() {
    	return gitlabConfig;
    }
    
    private String u(String k) {
        return Escaper.urlEncode(k, "");
    }
}
