package minerva.user;

import java.util.ArrayList;
import java.util.List;

import minerva.export.ExportUserSettings;

public class UserSettings {
    private final List<String> favorites = new ArrayList<>();
    private ExportUserSettings export;
    
    public List<String> getFavorites() {
        return favorites;
    }

    public ExportUserSettings getExport() {
        return export;
    }

    public void setExport(ExportUserSettings export) {
        this.export = export;
    }
}
