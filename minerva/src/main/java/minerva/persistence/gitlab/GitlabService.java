package minerva.persistence.gitlab;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;

import github.soltaufintel.amalia.web.config.AppConfig;

public class GitlabService {
    private final String gitlabUrl;
    private final String project;
    private final String user;
    private final String password;
    
    public GitlabService(GitlabUser user) {
        this(new AppConfig(), user.getLogin(), user.getPassword());
    }
    
    public GitlabService(AppConfig config, String user, String password) {
        this.gitlabUrl = config.get("gitlab.url");
        this.project = config.get("gitlab.project");
        this.user = user;
        this.password = password;
    }
    
    /**
     * @return email address of user. Possibly the mail address must be configured in Gitlab as public mail.
     * @throws GitLabApiException if login fails
     */
    public String login() throws GitLabApiException {
        try (GitLabApi g = GitLabApi.oauth2Login(gitlabUrl, user, password)) {
            String mail = g.getUserApi().getCurrentUser().getEmail();
            if (mail == null || mail.isEmpty()) {
                return user + "@minerva.de";
            }
            return mail;
        }
    }
    
    public List<String> getAllBranches() {
        try (GitLabApi g = GitLabApi.oauth2Login(gitlabUrl, user, password)) {
            return g.getRepositoryApi().getBranches(project).stream().map(i -> i.getName()).sorted().collect(Collectors.toList());
        } catch (GitLabApiException e) {
            throw new RuntimeException("Error reading all Git branches", e);
        }
    }
}
