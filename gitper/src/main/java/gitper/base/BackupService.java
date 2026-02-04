package gitper.base;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import gitper.Workspace;
import gitper.access.CommitMessage;
import gitper.access.DirAccess;

/**
 * Use only in Gitlab mode!
 */
public class BackupService {
    
    private BackupService() {
    }

    /**
     * @param backupItems items to be backuped
     * @param workspace -
     * @return message
     */
    public static String backup(List<BackupItem> backupItems, Workspace workspace) {
        // folder in workspace of current user:
        File backupDir = new File(workspace.getFolder(), "backup");
        Map<String, String> map = new HashMap<>();
        for (BackupItem file : backupItems) {
            String dn = file.getFilename();
            if (dn == null || dn.contains("\\") || dn.contains("/")) {
                throw new RuntimeException("Error in backup. Illegal filename: " + dn);
            }
            String content = file.getContent();
            if (content == null) {
                Logger.debug("No backup for non existing file: " + dn);
            } else {
                File target = new File(backupDir, dn);
                if (!target.isFile() || !content.equals(FileService.loadPlainTextFile(target))) {
                    map.put(target.getAbsolutePath(), content);
                    Logger.debug("File backup: " + dn + " -> " + target.getAbsolutePath());
                } else {
                    Logger.debug("Target file is already uptodate: " + target.getAbsolutePath());
                }
            }
        }
        if (map.isEmpty()) {
            return "files not found or not changed -> no backup!";
        }
        workspace.dao().saveFiles(map, new CommitMessage("backup"), workspace);
        return "backup made for " + map.size() + " file" + (map.size() == 1 ? "" : "s");
    }

    /**
     * Copy directory srcDir to targetDir and save targetDir to Gitlab.
     * @param srcDir non Gitlab dir
     * @param targetDir Gitlab dir (will be replaced by srcDir)
     * @param commitMessage -
     * @param workspace -
     */
    public static void archive(File srcDir, File targetDir, String commitMessage, Workspace workspace) {
        try {
            FileUtils.deleteDirectory(targetDir);
            FileUtils.copyDirectory(srcDir, targetDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        Map<String, String> files = new HashMap<>();
        collectFiles(targetDir, files);
        workspace.dao().saveFiles(files, new CommitMessage(commitMessage), workspace);
    }
    
    public static void collectFiles(File dir, Map<String, String> files) {
        for (String dn : FileService.loadFilenames(dir)) {
            var file = new File(dir, dn);
            if (file.isFile()) {
                files.put(file.getAbsolutePath(), DirAccess.IMAGE);
            } else if (file.isDirectory()) {
                collectFiles(file, files); // recursive
            }
        }
    }
}
