package minerva.persistence.gitlab;

import minerva.user.User;

public class GitlabUser extends User {
    /** usually empty */
    private final transient String password;
    private transient String accessToken;
    private transient String refreshToken;
    
    public GitlabUser(String login, String password) {
        setLogin(login);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
