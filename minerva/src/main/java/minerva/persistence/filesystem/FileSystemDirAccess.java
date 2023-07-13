package minerva.persistence.filesystem;

import minerva.access.AbstractDirAccess;
import minerva.model.WorkspaceSO;

public class FileSystemDirAccess extends AbstractDirAccess {

    @Override
    public void initWorkspace(WorkspaceSO workspace, boolean force) {
        //
    }

    @Override
    public void createBranch(WorkspaceSO workspace, String newBranch, String commit) {
        throw new UnsupportedOperationException("Create branch not supported");
    }
}
