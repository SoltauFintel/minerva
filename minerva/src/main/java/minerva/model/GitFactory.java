package minerva.model;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gitlab4j.api.Constants.TokenType;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.persistence.gitlab.GitlabUser;
import minerva.user.User;

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
    public static GitLabApi getGitLabApi(GitlabUser user) throws GitLabApiException {
        String gitlabUrl = new AppConfig().get("gitlab.url");
        if (user.getAccessToken() == null) {
            Logger.debug("GitLabApi via login+password");
            return GitLabApi.oauth2Login(gitlabUrl, user.getLogin(), user.getPassword());
        } else {
            Logger.debug("GitLabApi via access token");
            return new GitLabApi(gitlabUrl, TokenType.OAUTH2_ACCESS, user.getAccessToken());
        }
    }
    
    public static void logout(User pUser) {
        if (pUser instanceof GitlabUser user) {
            if (user.getAccessToken() == null) {
                return;
            }
            AppConfig cfg = new AppConfig();
            String gitlabUrl = cfg.get("gitlab.url");
            String appId = cfg.get("gitlab-appid");
            String secret = cfg.get("gitlab-secret");
            String params = "client_id=" + u(appId) //
                    + "&client_secret=" + u(secret) //
                    + "&token=" + u(user.getAccessToken());
            String r = new REST(gitlabUrl + "/oauth/revoke").post(params).response();
            if ("{}".equals(r)) {
                Logger.info("Gitlab revoke ok");
            } else {
                Logger.warn("Gitlab revoke failed");
            }
            user.setAccessToken(null);
            user.setRefreshToken(null);
        }
    }

    private static String u(String k) {
        return Escaper.urlEncode(k, "");
    }

    /**
     * Login first time to Gitlab with access token
     * @param accessToken
     * @return GitLabApi
     */
    public static GitLabApi initWithAccessToken(String accessToken) {
        String gitlabUrl = new AppConfig().get("gitlab.url");
        Logger.debug("GitLabApi via access token");
        return new GitLabApi(gitlabUrl, TokenType.OAUTH2_ACCESS, accessToken);
    }
    
    /**
     * Git access
     * @param user -
     * @return UsernamePasswordCredentialsProvider
     */
    public static UsernamePasswordCredentialsProvider getUsernamePasswordCredentialsProvider(GitlabUser user) {
        if (user.getAccessToken() == null) {
            Logger.debug(user.getLogin() + " | Git access with login and password");
            return new UsernamePasswordCredentialsProvider(user.getLogin(), user.getPassword());
        } else {
            Logger.debug(user.getLogin() + " | Git access with Gitlab oauth2 access token");
            return new UsernamePasswordCredentialsProvider("oauth2", user.getAccessToken());
        }
    }

    /**
     * Git access
     * @param url -
     * @param user -
     * @return modified url
     */
    public static String handleUrl(String url, GitlabUser user) {
        if (user.getAccessToken() != null) {
            // TODO auch "https://" unterstützen
            return "http://gitlab-ci-token:" + Escaper.urlEncode(user.getAccessToken(), "") + "@"
                    + url.substring("http://".length());
        }
        return url;
    }
}
