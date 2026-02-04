package gitper.base;

import github.soltaufintel.amalia.base.FileService;

public class JsonBackupItem implements BackupItem {
    private final String dn;
    private final String json;
    
    public JsonBackupItem(String dn, Object data) {
        this.dn = dn;
        json = FileService.prettyJSON(data);
    }
    
    @Override
    public String getContent() {
        return json;
    }

    @Override
    public String getFilename() {
        return dn;
    }
}
