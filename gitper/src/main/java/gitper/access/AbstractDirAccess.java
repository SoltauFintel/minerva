package gitper.access;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import gitper.Workspace;
import gitper.movefile.ChangeFile;
import gitper.movefile.IMoveFile;
import gitper.movefile.MoveFile;

/**
 * File based directory access
 */
public abstract class AbstractDirAccess implements DirAccess {

    @Override
    public List<String> getAllFolders(String folder) {
        return FileService.listFolders(new File(folder));
    }

    @Override
    public Map<String, String> loadFiles(Set<String> filenames) {
        Map<String, String> ret = new HashMap<>();
        for (String filename : filenames) {
            ret.put(filename, FileService.loadPlainTextFile(new File(filename)));
        }
        return ret;
    }

    @Override
    public Map<String, String> loadAllFiles(String folder) {
        return loadAllFiles(folder, file -> file.isFile() && !file.getName().startsWith("."));
    }

    @Override
    public Map<String, String> loadAllFiles(String folder, String postfix) {
        return loadAllFiles(folder, file -> file.isFile() && !file.getName().startsWith(".") && file.getName().endsWith(postfix));
    }
    
    private Map<String, String> loadAllFiles(String folder, Predicate<File> checkFile) {
        Map<String, String> ret = new HashMap<>();
        File[] files = new File(folder).listFiles();
        if (files != null) {
            for (File file : files) {
                if (checkFile.test(file)) {
                    ret.put(file.getName(), FileService.loadPlainTextFile(new File(folder + "/" + file.getName())));
                }
            }
        }
        return ret;
    }

    @Override
    public void saveFiles(Map<String, String> files, CommitMessage commitMessage, Workspace workspace) {
        for (Entry<String, String> e : files.entrySet()) {
            File file = new File(e.getKey());
            if (e.getValue() == null) {
                file.delete();
            } else if (IMAGE.equals(e.getValue())) {
                // do nothing
            } else {
                FileService.savePlainTextFile(file, e.getValue());
            }
        }
    }
    
    @Override
    public void deleteFiles(Set<String> filenames, CommitMessage commitMessage, Workspace workspace, List<String> cantBeDeleted) {
        if (cantBeDeleted == null) {
            throw new IllegalArgumentException("cantBeDeleted must not be null");
        }
        Set<String> addList = new HashSet<>();
        Set<String> killList = new HashSet<>();
        for (String dn : filenames) {
            boolean ok = false;
            if (dn.endsWith("/*")) { // folder
                String fn = dn.substring(0, dn.length() - "/*".length());
                File folder = new File(fn);
                if (folder.isDirectory()) {
                    deleteFolder(folder, addList, cantBeDeleted, false);
                    ok = true;
                    killList.add(dn);
                } else if (!folder.isFile()) {
                    ok = true; // Datei/Ordner existiert nicht. Nicht als Fehler werten.
                }
            } else if (dn.endsWith("/**")) { // folder and all subfolders
                String fn = dn.substring(0, dn.length() - "/**".length());
                File folder = new File(fn);
                if (folder.isDirectory()) {
                    deleteFolder(folder, addList, cantBeDeleted, true);
                    ok = true;
                    killList.add(dn);
                } else if (!folder.isFile()) {
                    ok = true; // Datei/Ordner existiert nicht. Nicht als Fehler werten.
                }
            } else { // file
                File file = new File(dn);
                if (file.isFile()) {
                    ok = file.delete();
                } else if (!file.isDirectory()) {
                    ok = true; // Datei/Ordner existiert nicht. Nicht als Fehler werten.
                }
            }
            if (!ok) {
                cantBeDeleted.add(dn);
            }
        }
        filenames.removeAll(killList);
        filenames.addAll(addList);
    }

    private void deleteFolder(File folder, Set<String> addList, List<String> cantBeDeleted, boolean deleteSubfolders) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (deleteSubfolders) {
                        deleteFolder(file, addList, cantBeDeleted, deleteSubfolders);
                    } else {
                        Logger.error("[deleteFolder] Unexpected folder: " + file.getAbsolutePath());
                        cantBeDeleted.add(file.getAbsolutePath());
                    }
                } else {
                    if (file.delete()) {
                        addList.add(file.getAbsolutePath().replace("\\", "/"));
                    } else {
                        cantBeDeleted.add(file.getAbsolutePath());
                    }
                }
            }
        }
        folder.delete();
    }
    
    @Override
    public void moveFiles(List<IMoveFile> files, CommitMessage commitMessage, Workspace workspace) {
        for (IMoveFile f : files) {
            if (f instanceof MoveFile mf) {
                File source = new File(mf.getOldFile());
                File target = new File(mf.getNewFile());
                if (!source.exists()) {
                    Logger.warn("(move-to-book) " + source.getAbsolutePath() + " not found. -> skipped");
                } else if (source.isDirectory()) {
                    try {
                        FileUtils.moveDirectory(source, target);
                    } catch (IOException e) {
                        Logger.error(e, "Error moving folder " + source.getAbsolutePath());
                        throw new RuntimeException("Error moving images folder to other book! Please pull workspace!");
                    }
                } else if (source.isFile()) {
                    target.getParentFile().mkdirs();
                    source.renameTo(target);
                }
            } else if (f instanceof ChangeFile cf) {
                FileService.savePlainTextFile(new File(cf.getFilename()), cf.getContent());
            } else if (f != null) {
                Logger.error("Unknown IMoveFile class: " + f.getClass().getName());
            }
        }
    }
    
    @Override
    public CommitHash getCommitHash(Workspace workspace) {
        return new CommitHash();
    }
    
    @Override
    public List<String> copyFiles(String bookFolder, String source, String target) {
        List<String> ret = new ArrayList<>();
        File[] files = new File(bookFolder + source).listFiles();
        if (files != null) {
            File targetDir = new File(bookFolder, target);
            targetDir.mkdirs();
            for (File file : files) {
                FileService.copyFile(file, targetDir);
                ret.add(target + "/" + file.getName());
            }
        }
        return ret;
    }

    // see also FileService.loadFilenames()
    @Override
    public Set<String> getFilenames(String folder) {
        var dir = new File(folder);
        if (!dir.isDirectory()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        Set<String> ret = new TreeSet<>();
        for (File file : files) {
            ret.add(file.getName());
        }
        return ret;
    }
}
