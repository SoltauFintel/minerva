package minerva.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minerva.base.StringService;
import minerva.model.WorkspaceSO;

public class SimpleDirAccess {
    private final DirAccess dao;
    private final WorkspaceSO workspace;

    public SimpleDirAccess(DirAccess dao, WorkspaceSO workspace) {
        this.dao = dao;
        this.workspace = workspace;
    }
    
    public DirAccess dao() {
        return dao;
    }

    public <T> void save(String id, T data, Set<String> images, CommitMessage commitMessage, String dir) {
        Map<String, String> files = new HashMap<>();
        files.put(dir + "/" + id + ".json", StringService.prettyJSON(data));
        images.forEach(dn -> files.put(dir + "/img/" + id + "/" + dn, DirAccess.IMAGE));
        dao.saveFiles(files, commitMessage, workspace);
    }

    public void delete(String id, CommitMessage commitMessage, String dir) {
        Set<String> filenames = new HashSet<>();
        filenames.add(dir + "/" + id + ".json");
        List<String> cantBeDeleted = new ArrayList<>();
        dao.deleteFiles(filenames, commitMessage, workspace, cantBeDeleted);
        if (!cantBeDeleted.isEmpty()) {
            throw new RuntimeException("Item can not be deleted!");
        }
    }
}
