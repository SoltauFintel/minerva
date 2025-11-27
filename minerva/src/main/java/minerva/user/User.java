package minerva.user;

import java.util.ArrayList;
import java.util.List;

import gitper.GitlabConfig;
import minerva.MinervaWebapp;
import minerva.config.MinervaGitlabConfig;
import minerva.export.ExportUserSettings;
import minerva.user.quickbuttons.Quickbutton;

public class User implements gitper.User {
    private String login;
    private String realName;
    private String mailAddress;
    private boolean exportAllowed;
    /** The user has a preferred language. That will be used for the GUI. */
    private String guiLanguage = MinervaWebapp.factory() == null ? null : MinervaWebapp.factory().getInitialLanguage();
    /** The current language tab for a page. */
    private String pageLanguage = MinervaWebapp.factory() == null ? null : MinervaWebapp.factory().getInitialLanguage();
    private ExportUserSettings export;
    private final List<String> favorites = new ArrayList<>();
    private final List<String> watchlist = new ArrayList<>();
    /** only used with Gitlab backend: all branch names where file-system mode is temporarily active */
    private final List<String> delayedPush = new ArrayList<>();
    private String lastEditedPage;
    private List<String> taskPriorities;
    private boolean showAllPages = false;
    private String attachmentCategory;
    private String publishReleaseNumber;
    private String customerMode;
    /** Business model: selected database */
    private String database;
    private final List<Quickbutton> quickbuttons = new ArrayList<>();
    private boolean showQuickbuttons = false;
    private String initialien = "";
    /** hat besondere Rechte für diese Kunden (getrennt mit ",") */
    private String customerRights;
    private String rolloutConfig;
    private String rolloutCustomer;
    private String targetBranch;
    
    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getMailAddress() {
        return mailAddress;
    }

    @Override
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

    @Override
    public String getRealName() {
        return realName == null ? "" : realName;
    }

    public void setRealName(String realName) {
        this.realName = realName == null ? "" : realName;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public List<String> getWatchlist() {
        return watchlist;
    }

    @Override
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

    public String getPublishReleaseNumber() {
        return publishReleaseNumber;
    }

    public void setPublishReleaseNumber(String publishReleaseNumber) {
        this.publishReleaseNumber = publishReleaseNumber;
    }

    public String getCustomerMode() {
        return customerMode;
    }

    public void setCustomerMode(String customerMode) {
        this.customerMode = customerMode;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public GitlabConfig getGitlabConfig() {
        return new MinervaGitlabConfig();
    }

    public List<Quickbutton> getQuickbuttons() {
        return quickbuttons;
    }
    
    public Quickbutton getQuickbutton(String id) {
        for (var q : quickbuttons) {
            if (q.getId().equals(id)) {
                return q;
            }
        }
        throw new RuntimeException("Quick button does not exist!");
    }

    public boolean isShowQuickbuttons() {
        return showQuickbuttons;
    }

    public void setShowQuickbuttons(boolean showQuickbuttons) {
        this.showQuickbuttons = showQuickbuttons;
    }

    public String getInitialien() {
        return initialien;
    }

    public void setInitialien(String initialien) {
        this.initialien = initialien;
    }

    public String getCustomerRights() {
        return customerRights;
    }

    public void setCustomerRights(String customerRights) {
        this.customerRights = customerRights;
    }

    public String getRolloutConfig() {
        return rolloutConfig;
    }

    public void setRolloutConfig(String rolloutConfig) {
        this.rolloutConfig = rolloutConfig;
    }

    public String getRolloutCustomer() {
        return rolloutCustomer;
    }

    public void setRolloutCustomer(String rolloutCustomer) {
        this.rolloutCustomer = rolloutCustomer;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }
}
