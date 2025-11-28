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

    /**
     * Copy director srcDir to targetDir and save targetDir to Gitlab.
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
                files.put(file.getAbsolutePath().replace("\\", "/"), DirAccess.IMAGE);
            } else if (file.isDirectory()) {
                collectFiles(file, files); // recursive
            }
        }
    }
}
