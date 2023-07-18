package minerva.user;

import minerva.MinervaWebapp;

public class User {
    private final String login;
    private final String folder;
    /** The user has a preferred language. That will be used for the GUI. */
    private String guiLanguage = MinervaWebapp.factory().getLanguages().get(0);
    /** The current language tab for a page. */
    private String pageLanguage = MinervaWebapp.factory().getLanguages().get(0);
    
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

    public String getGuiLanguage() {
        return guiLanguage;
    }

    public void setGuiLanguage(String language) {
        this.guiLanguage = language;
    }

    public String getPageLanguage() {
        return pageLanguage;
    }

    public void setPageLanguage(String language) {
        this.pageLanguage = language;
    }

    // TODO bei GitlabUser könnte man hier den richtigen Namen setzen
    public String getRealName() {
        return getLogin();
    }
}
