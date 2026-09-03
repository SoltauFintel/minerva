package gitper;

import gitper.access.DirAccess;

public class WorkspaceAdapter implements Workspace {

    @Override
    public String getBranch() {
        return null;
    }

    @Override
    public String getFolder() {
        return null;
    }

    @Override
    public User user() {
        return null;
    }

    @Override
    public DirAccess dao() {
        return null;
    }

    @Override
    public void onPush() {
    }

    @Override
    public void pull() {
    }
}
