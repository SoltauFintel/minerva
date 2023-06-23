package minerva.persistence.gitlab;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;

import minerva.MinervaWebapp;
import minerva.model.GitFactory;

public class GitlabService {
    private final String project;
    private final GitlabUser user;
    
    public GitlabService(GitlabUser user) {
        this.project = MinervaWebapp.factory().getConfig().getGitlabProject();
        this.user = user;
    }
    
    /**
     * @return email address of user. Possibly the mail address must be configured in Gitlab as public mail.
     * @throws GitLabApiException if login fails
     */
    public String login() throws GitLabApiException {
        try (GitLabApi g = GitFactory.getGitLabApi(user)) {
            String mail = g.getUserApi().getCurrentUser().getEmail();
            if (mail == null || mail.isEmpty()) {
                return user.getLogin() + "@minerva.de";
            }
            return mail;
        }
    }
    
    public List<String> getAllBranches() {
        try (GitLabApi g = GitFactory.getGitLabApi(user)) {
            return g.getRepositoryApi().getBranches(project).stream()
                    .map(i -> i.getName())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (GitLabApiException e) {
            throw new RuntimeException("Error reading all Git branches", e);
        }
    }
}
