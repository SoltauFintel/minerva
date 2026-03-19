package gitper.base;

import java.io.File;

import github.soltaufintel.amalia.base.FileService;

public class FileBackupItem implements BackupItem {
    private final File file;
    
    public FileBackupItem(File file) {
        this.file = file;
    }
    
    @Override
    public String getContent() {
        return FileService.loadPlainTextFile(file);
    }

    @Override
    public String getFilename() {
        return file.getName();
    }
}
