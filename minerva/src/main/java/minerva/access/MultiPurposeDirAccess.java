package minerva.access;

import java.util.HashMap;
import java.util.HashSet;
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

    public void save(String filename, String text, String commitMessage, WorkspaceSO workspace) {
        Map<String, String> files = new HashMap<>();
        files.put(filename, text);    
        access.saveFiles(files, commitMessage, workspace);
    }

    public <T> void save(String filename, T data, String commitMessage, WorkspaceSO workspace) {
        save(filename, StringService.prettyJSON(data), commitMessage, workspace);
    }

    public <T> T load(String dn, Class<T> type) {
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
}
