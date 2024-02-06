package minerva.access;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public <T> void save(String id, T data, CommitMessage commitMessage, String dir) {
        new MultiPurposeDirAccess(dao).save(dir + "/" + id + ".json", data, commitMessage, workspace);
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
