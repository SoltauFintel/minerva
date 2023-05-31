package minerva.model;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.git.GitService;
import minerva.persistence.gitlab.GitlabUser;

public class GitlabRepositorySO {
	private final GitlabSystemSO gitlab;
	private final String project;

	public GitlabRepositorySO(GitlabSystemSO gitlab, String project) {
		this.gitlab = gitlab;
		this.project = project;
	}
	
	public void pull(WorkspaceSO workspace, boolean forceClone) {
		File workspaceFolder = new File(workspace.getFolder());
		if (forceClone) {
			FileService.deleteFolder(workspaceFolder);
			if (workspaceFolder.exists()) {
				Logger.error("Workspace folder can't be deleted: " + workspaceFolder.getAbsolutePath());
				throw new RuntimeException("Workspace could not be deleted! Force clone failed.");
			}
		}
		String branch = workspace.getBranch();
		GitlabUser user = (GitlabUser) workspace.getUser().getUser();
		
		GitService git = new GitService(workspaceFolder);
		if (new File(workspaceFolder, ".git").exists()) {
			if (!git.getCurrentBranch().equals(branch)) {
				git.fetch(user.getLogin(), user.getPassword());
				git.switchToBranch(branch);
			}
			try {
				git.pull(user.getLogin(), user.getPassword());
			} catch (Exception e) {
				Logger.error(e);
				pull(workspace, true);
				return;
			}
		} else {
			workspaceFolder.mkdirs();
			String url = gitlab.getUrl() + "/" + project;
			Logger.info((forceClone ? "force " : "") + "clone from " + url
					+ " into " + workspaceFolder.getAbsolutePath());
			git.clone(url, user.getLogin(), user.getPassword(), branch, false);
		}
	}
	
	public void push(String commitMessage, WorkspaceSO workspace,
			Set<String> addFilenames, Set<String> removeFilenames, SaveProcedure saveFiles) {
		GitlabPushTransaction tx = new GitlabPushTransaction(this, commitMessage, workspace);
		tx.createWorkBranch();
		try {
			tx.switchToWorkBranch();
			saveFiles.saveToDisk();
			if (!tx.commitAndPush(addFilenames, removeFilenames)) {
				return;
			}
			tx.doMergeRequest();
		} finally {
			tx.finish();
		}
	}

	public interface SaveProcedure {
		void saveToDisk();
	}

	public String getGitlabSystemUrl() {
		return gitlab.getUrl();
	}
	
	public String getProject() {
		return project;
	}
	
	public String getProjectUrl() {
		return gitlab.getUrl() + "/" + project;
	}

	public List<String> getBranches(WorkspaceSO workspace) {
		return new GitService(new File(workspace.getFolder())).getBranchNames();
	}
}
