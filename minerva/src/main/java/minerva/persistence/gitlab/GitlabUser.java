package minerva.persistence.gitlab;

import minerva.user.User;

public class GitlabUser extends User {
    private final String password;
    private String mail;
    private String accessToken;
    private String refreshToken;
    
    public GitlabUser(String login, String password) {
        super(login, login /*user folder is always the login*/);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
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
