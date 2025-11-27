package gitper.persistence.gitlab;

import gitper.Workspace;

// new UserMessage("notExistingBranch", workspace)
public class NotExistingBranchException extends RuntimeException {
    private final Workspace workspace;
    
    public NotExistingBranchException(Workspace workspace) {
        super("Not existing branch");
        this.workspace = workspace;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
}
