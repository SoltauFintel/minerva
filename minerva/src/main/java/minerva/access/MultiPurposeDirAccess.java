package minerva.access;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import minerva.base.StringService;
import minerva.model.WorkspaceSO;

/**
 * Adds more generic functions to less-methods DirAccess interface
 */
public class MultiPurposeDirAccess {
    private final DirAccess access;

    public MultiPurposeDirAccess(DirAccess access) {
        this.access = access;
    }

    public void save(String filename, String text, CommitMessage commitMessage, WorkspaceSO workspace) {
        Map<String, String> files = new HashMap<>();
        files.put(filename, text);    
        access.saveFiles(files, commitMessage, workspace);
    }

    public <T> void save(String filename, T data, CommitMessage commitMessage, WorkspaceSO workspace) {
        save(filename, StringService.prettyJSON(data), commitMessage, workspace);
    }

    public String load(String dn) {
        Set<String> filenames = new HashSet<>();
        filenames.add(dn);
        Map<String, String> files = access.loadFiles(filenames);
        return files.get(dn);
    }
    
    public <T> T load(String dn, Class<T> type) {
        if (!new File(dn).isFile()) {
            return null;
        }
        Set<String> filenames = new HashSet<>();
        filenames.add(dn);
        Map<String, String> files = access.loadFiles(filenames);
        String json = files.get(dn);
        if (json == null) {
            Logger.error("JSON is null for file: " + dn);
            return null;
        }
        return new Gson().fromJson(json, type);
    }

    /**
     * @param filename full filename
     * @param commitMessage -
     * @param workspace -
     * @return true: success, false: error deleting that file
     */
    public boolean delete(String filename, CommitMessage commitMessage, WorkspaceSO workspace) {
        List<String> cantBeDeleted = new ArrayList<>();
        workspace.dao().deleteFiles(Set.of(filename), commitMessage, workspace, cantBeDeleted);
        return cantBeDeleted.isEmpty();
    }
}
