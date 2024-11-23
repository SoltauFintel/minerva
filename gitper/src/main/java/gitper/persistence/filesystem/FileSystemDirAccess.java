package gitper.persistence.filesystem;

import java.util.ArrayList;
import java.util.List;

import gitper.User;
import gitper.Workspace;
import gitper.access.AbstractDirAccess;

public class FileSystemDirAccess extends AbstractDirAccess {

    @Override
    public void initWorkspace(Workspace workspace, boolean force) {
        //
    }

    @Override
    public void createBranch(Workspace workspace, String newBranch, String commit) {
        throw new UnsupportedOperationException("Create branch not supported");
    }

    @Override
    public List<String> getBranchNames(Workspace workspace) {
        return new ArrayList<>();
    }

	@Override
	public void mergeBranch(String sourceBranch, String targetBranch, User user) {
        throw new UnsupportedOperationException("Merge branch not supported");
	}
}
