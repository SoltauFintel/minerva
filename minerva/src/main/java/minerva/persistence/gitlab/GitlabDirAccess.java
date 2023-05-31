package minerva.persistence.gitlab;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minerva.MinervaWebapp;
import minerva.access.AbstractDirAccess;
import minerva.model.GitlabRepositorySO;
import minerva.model.WorkspaceSO;

public class GitlabDirAccess extends AbstractDirAccess {
	private final GitlabRepositorySO repo = MinervaWebapp.factory().getGitlabRepository();

	@Override
	public void initWorkspace(WorkspaceSO workspace, boolean forceClone) {
		repo.pull(workspace, forceClone);
	}

	@Override
	public void saveFiles(Map<String, String> files, String commitMessage, WorkspaceSO workspace) {
		repo.push(commitMessage, workspace, files.keySet(), emptySet(),
				() -> super.saveFiles(files, commitMessage, workspace));
	}

	@Override
	public void deleteFiles(Set<String> filenames, String commitMessage, WorkspaceSO workspace,
			List<String> cantBeDeleted) {
		repo.push(commitMessage, workspace, emptySet(), filenames,
				() -> super.deleteFiles(filenames, commitMessage, workspace, cantBeDeleted));
	}

	private HashSet<String> emptySet() {
		return new HashSet<>();
	}
}
