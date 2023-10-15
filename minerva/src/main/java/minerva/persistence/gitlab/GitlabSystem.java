package minerva.persistence.gitlab;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import minerva.user.User;

public class GitlabSystem {
    private final String url;
    
    public GitlabSystem(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }

    /**
     * @param user -
     * @return Gitlab user mail address, null if can't log in
     */
    public String login(User user) {
        try (GitLabApi g = GitFactory.getGitLabApi(user)) {
            String mail = g.getUserApi().getCurrentUser().getEmail();
            if (mail == null || mail.isEmpty()) {
                return user + "@minerva.de";
            }
            return mail;
        } catch (GitLabApiException e) {
            Logger.error(e);
            return null;
        }
    }
}
