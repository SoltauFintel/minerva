package minerva.user;

import java.util.ArrayList;
import java.util.List;

import minerva.export.ExportUserSettings;

public class UserSettings {
    private String guiLanguage;
    private String pageLanguage;
    private ExportUserSettings export;
    private final List<String> favorites = new ArrayList<>();
    private final List<String> watchlist = new ArrayList<>();
    
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
}
