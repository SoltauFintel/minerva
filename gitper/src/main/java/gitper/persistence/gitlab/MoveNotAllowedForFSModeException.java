package gitper.persistence.gitlab;

import gitper.Workspace;

public class MoveNotAllowedForFSModeException extends RuntimeException {
	private final Workspace workspace;
	
	public MoveNotAllowedForFSModeException(Workspace workspace) {
		super("Move not allowed for FS mode");
		this.workspace = workspace;
	}

	public Workspace getWorkspace() {
		return workspace;
	}
}
