package gitper.base;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import gitper.Workspace;
import gitper.access.CommitMessage;

public class BackupService {
    
    private BackupService() {
    }

    /**
     * Call only in Gitlab mode!
     * @param files to be backuped
     * @param backupDir target directory in workspace
     * @param workspace -
     * @return message
     */
    public static String backup(List<File> files, File backupDir, Workspace workspace) {
        Map<String, String> map = new HashMap<>();
        for (File file : files) {
            if (file.isFile()) {
                String content = FileService.loadPlainTextFile(file);
                File target = new File(backupDir, file.getName());
                if (!target.isFile() || !content.equals(FileService.loadPlainTextFile(target))) {
                    map.put(target.getAbsolutePath(), content);
                    Logger.debug("File backup: " + file.getAbsolutePath() + " -> " + target.getAbsolutePath());
                } else {
                    Logger.debug("Target file is already uptodate: " + target.getAbsolutePath());
                }
            } else {
                Logger.debug("File not found: " + file.getAbsolutePath());
            }
        }
        if (map.isEmpty()) {
            return "files not found or not changed -> no backup!";
        }
        workspace.dao().saveFiles(map, new CommitMessage("backup"), workspace);
        return "backup made for " + map.size() + " file" + (map.size() == 1 ? "" : "s");
    }
}
