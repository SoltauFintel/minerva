package gitper.base;

public interface BackupItem {

    /**
     * @return null: item does not exist, do not backup
     */
    String getContent();
    
    String getFilename();
}
