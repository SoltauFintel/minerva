package minerva.user;

import java.util.ArrayList;
import java.util.List;

import minerva.MinervaWebapp;
import minerva.export.ExportUserSettings;

public class User {
    private String login;
    private String realName;
    private String mailAddress;
    private boolean exportAllowed;
    /** The user has a preferred language. That will be used for the GUI. */
    private String guiLanguage = MinervaWebapp.factory().getLanguages().get(0);
    /** The current language tab for a page. */
    private String pageLanguage = MinervaWebapp.factory().getLanguages().get(0);
    private ExportUserSettings export;
    private final List<String> favorites = new ArrayList<>();
    private final List<String> watchlist = new ArrayList<>();
    /** only used with Gitlab backend: all branch names where file-system mode is temporarily active */
    private final List<String> delayedPush = new ArrayList<>();
    private String lastEditedPage;
    private List<String> taskPriorities;
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getGuiLanguage() {
        return guiLanguage;
    }

    public void setGuiLanguage(String guiLanguage) {
        this.guiLanguage = guiLanguage;
    }

    public String getPageLanguage() {
        return pageLanguage;
    }

    public void setPageLanguage(String pageLanguage) {
        this.pageLanguage = pageLanguage;
    }

    public ExportUserSettings getExport() {
        return export;
    }

    public void setExport(ExportUserSettings export) {
        this.export = export;
    }

    public String getLastEditedPage() {
        return lastEditedPage;
    }

    public void setLastEditedPage(String lastEditedPage) {
        this.lastEditedPage = lastEditedPage;
    }

    public boolean isExportAllowed() {
        return exportAllowed;
    }

    public void setExportAllowed(boolean exportAllowed) {
        this.exportAllowed = exportAllowed;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public List<String> getWatchlist() {
        return watchlist;
    }

    public List<String> getDelayedPush() {
        return delayedPush;
    }

    public List<String> getTaskPriorities() {
        return taskPriorities;
    }

    public void setTaskPriorities(List<String> taskPriorities) {
        this.taskPriorities = taskPriorities;
    }
}
