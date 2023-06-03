package minerva.user;

import minerva.MinervaWebapp;

public class User {
    private final String login;
    private final String folder;
    /** Einstellung welche Sprache angezeigt wird */
    private String language = MinervaWebapp.factory().getLanguages().get(0);
    
    public User(String login, String folder) {
        this.login = login;
        this.folder = folder;
    }

    public String getLogin() {
        return login;
    }
    
    public String getFolder() {
        return folder;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    // TODO bei GitlabUser könnte man hier den richtigen Namen setzen
    public String getRealName() {
        return getLogin();
    }
}
