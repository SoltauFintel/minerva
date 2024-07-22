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
    private String guiLanguage = MinervaWebapp.factory().getInitialLanguage();
    /** The current language tab for a page. */
    private String pageLanguage = MinervaWebapp.factory().getInitialLanguage();
    private ExportUserSettings export;
    private final List<String> favorites = new ArrayList<>();
    private final List<String> watchlist = new ArrayList<>();
    /** only used with Gitlab backend: all branch names where file-system mode is temporarily active */
    private final List<String> delayedPush = new ArrayList<>();
    private String lastEditedPage;
    private List<String> taskPriorities;
    private boolean showAllPages = false;
    private String attachmentCategory;
    
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
        return validLang(guiLanguage);
    }

    public void setGuiLanguage(String guiLanguage) {
        if (getLanguages().contains(guiLanguage)) {
            this.guiLanguage = guiLanguage;
        }
    }

    public String getPageLanguage() {
        return validLang(pageLanguage);
    }

    public void setPageLanguage(String pageLanguage) {
        if (getLanguages().contains(pageLanguage)) {
            this.pageLanguage = pageLanguage;
        }
    }
    
    private String validLang(String theLanguage) {
        List<String> validLangs = getLanguages();
        for (String i : validLangs) {
            if (i.equals(theLanguage)) {
                return theLanguage;
            }
        }
        return validLangs.get(0);
    }

    private List<String> getLanguages() {
        return MinervaWebapp.factory().getConfig().getLanguages();
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
    
    @Override
    public String toString() {
        return "User "  + login;
    }

	public boolean isShowAllPages() {
		return showAllPages;
	}

	public void setShowAllPages(boolean showAllPages) {
		this.showAllPages = showAllPages;
	}

    public String getAttachmentCategory() {
        return attachmentCategory;
    }

    public void setAttachmentCategory(String attachmentCategory) {
        this.attachmentCategory = attachmentCategory;
    }
}
