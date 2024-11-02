package gitper.persistence.gitlab;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gitlab4j.api.Constants.TokenType;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.web.action.Escaper;
import gitper.GitlabConfig;
import gitper.User;

/**
 * Liefert je nachdem sich der Benutzer über Passwort oder OAuth2 angemeldet hat, das entsprechend
 * erstellte Objekt für Gitlab- oder Git-Access zurück.
 */
public class GitFactory {

    private GitFactory() {
    }

    /**
     * Gitlab access
     * @param user -
     * @return GitLabApi
     * @throws GitLabApiException
     */
    public static GitLabApi getGitLabApi(User user) throws GitLabApiException {
        String gitlabUrl = user.getGitlabConfig().getGitlabUrl();
        GitlabDataStore xu = new GitlabDataStore(user);
        String accessToken = xu.getAccessToken();
        if (accessToken == null) {
            Logger.debug("GitLabApi via login+password");
            return GitLabApi.oauth2Login(gitlabUrl, user.getLogin(), xu.getPassword());
        } else {
            Logger.debug("GitLabApi via access token");
            return new GitLabApi(gitlabUrl, TokenType.OAUTH2_ACCESS, accessToken);
        }
    }
    
    public static boolean logout(User user) {
        boolean revokeOk = false;
        GitlabDataStore xu = new GitlabDataStore(user);
        if (xu.getAccessToken() == null) {
            return false;
        }
        GitlabConfig gitlab = user.getGitlabConfig();
		String gitlabUrl = gitlab.getGitlabUrl();
        String appId = gitlab.getGitlabAppId();
        String secret = gitlab.getGitlabSecret();
        String params = "client_id=" + u(appId) //
                + "&client_secret=" + u(secret) //
                + "&token=" + u(xu.getAccessToken());
        String r = new REST(gitlabUrl + "/oauth/revoke").post(params).response();
        if ("{}".equals(r)) {
            revokeOk = true;
        } else {
            Logger.warn("Gitlab revoke failed");
        }
        xu.setAccessToken(null);
        xu.setRefreshToken(null);
        return revokeOk;
    }

    private static String u(String k) {
        return Escaper.urlEncode(k, "");
    }

    /**
     * Login first time to Gitlab with access token
     * @param accessToken -
     * @param gitlabUrl from config
     * @return GitLabApi
     */
    public static GitLabApi initWithAccessToken(String accessToken, String gitlabUrl) {
        Logger.debug("GitLabApi via access token");
        return new GitLabApi(gitlabUrl, TokenType.OAUTH2_ACCESS, accessToken);
    }
    
    /**
     * Git access
     * @param user -
     * @return UsernamePasswordCredentialsProvider
     */
    public static UsernamePasswordCredentialsProvider getUsernamePasswordCredentialsProvider(User user) {
        GitlabDataStore xu = new GitlabDataStore(user);
        String accessToken = xu.getAccessToken();
        if (accessToken == null) {
            Logger.debug(user.getLogin() + " | Git access with login and password");
            return new UsernamePasswordCredentialsProvider(user.getLogin(), xu.getPassword());
        } else {
            Logger.debug(user.getLogin() + " | Git access with Gitlab oauth2 access token");
            return new UsernamePasswordCredentialsProvider("oauth2", accessToken);
        }
    }

    /**
     * Git access
     * @param url -
     * @param user -
     * @return modified url
     */
    public static String handleUrl(String url, User user) {
        GitlabDataStore xu = new GitlabDataStore(user);
        String accessToken = xu.getAccessToken();
        if (accessToken != null) {
            return "http://gitlab-ci-token:" + Escaper.urlEncode(accessToken, "") + "@"
                    + url.substring("http://".length());
        }
        return url;
    }
}
