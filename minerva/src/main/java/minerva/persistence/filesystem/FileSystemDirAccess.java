package minerva.persistence.filesystem;

import java.util.ArrayList;
import java.util.List;

import minerva.access.AbstractDirAccess;
import minerva.model.WorkspaceSO;
import minerva.user.User;

public class FileSystemDirAccess extends AbstractDirAccess {

    @Override
    public void initWorkspace(WorkspaceSO workspace, boolean force) {
        //
    }

    @Override
    public void createBranch(WorkspaceSO workspace, String newBranch, String commit) {
        throw new UnsupportedOperationException("Create branch not supported");
    }

    @Override
    public List<String> getBranchNames(WorkspaceSO workspace) {
        return new ArrayList<>();
    }

    @Override
    public void mergeBranch(String sourceBranch, String targetBranch, User user) {
        throw new UnsupportedOperationException("Merge branch not supported");
    }
}
