package gitper;

import github.soltaufintel.amalia.spark.Context;
import gitper.persistence.gitlab.GitlabAuthService;

public interface GitperInterface {

    void login2(Context ctx, User user);
    
    void tosmap_add(String state, long t); // Tosmap.add()

    Object tosmap_pop(String state); // Tosmap.pop()
    
    /** returns GitLabApi */
    Object initWithAccessToken(String token); // GitFactory.initWithAccessToken()
    
    User createUser(String login);
    
    User loadUser(String login, boolean create, String mail); // UserAccess
    
    GitlabAuthService authService();
}
