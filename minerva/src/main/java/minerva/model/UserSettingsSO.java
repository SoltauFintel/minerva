package minerva.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import minerva.MinervaWebapp;
import minerva.base.FileService;
import minerva.export.ExportUserSettings;

public class UserSettingsSO {
    private transient String login;
    private String guiLanguage = "de";
    private String pageLanguage = "de";
    private ExportUserSettings export;
    private final List<String> favorites = new ArrayList<>();
    private final List<String> watchlist = new ArrayList<>();
    private String lastEditedPage;
    
    public static UserSettingsSO load(String login) {
        UserSettingsSO us;
        File file = file(login);
        if (file.isFile()) {
            us = FileService.loadJsonFile(file, UserSettingsSO.class);
        } else {
            us = new UserSettingsSO();
        }
        us.login = login;
        return us;
    }
    
    private static File file(String login) {
        // In the future, the settings may have to be saved in the workspace.
        return new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder() + "/" + login + "/user-settings.json");
    }
    
    private UserSettingsSO() {
    }
    
    public void save() {
        FileService.saveJsonFile(file(login), this);
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public ExportUserSettings getExport() {
        return export;
    }

    public void setExport(ExportUserSettings export) {
        this.export = export;
    }

    public String getPageLanguage() {
        return pageLanguage;
    }

    public void setPageLanguage(String pageLanguage) {
        this.pageLanguage = pageLanguage;
    }

    public String getGuiLanguage() {
        return guiLanguage;
    }

    public void setGuiLanguage(String guiLanguage) {
        this.guiLanguage = guiLanguage;
    }

    public List<String> getWatchlist() {
        return watchlist;
    }

    public String getLastEditedPage() {
        return lastEditedPage;
    }

    public void setLastEditedPage(String lastEditedPage) {
        this.lastEditedPage = lastEditedPage;
    }
}
