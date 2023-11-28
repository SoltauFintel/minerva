package minerva.persistence.gitlab;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.base.FileService;
import minerva.base.UserMessage;
import minerva.config.ICommit;
import minerva.model.WorkspaceSO;
import minerva.persistence.gitlab.git.GitService;
import minerva.user.User;

public class GitlabRepository {
    private final GitlabSystem gitlab;
    private final String project;

    public GitlabRepository(GitlabSystem gitlab, String project) {
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
        User user = workspace.getUser().getUser();
        
        GitService git = new GitService(workspaceFolder);
        if (new File(workspaceFolder, ".git").exists()) {
            if (!git.getCurrentBranch().equals(branch)) {
                git.fetch(user);
                git.switchToBranch(branch);
            }
            for (int trial = 1; trial <= 2; trial++) {
                try {
                    git.pull(user);
                    break;
                } catch (Exception e) {
                    Logger.error(e);
                    if (e.getCause() instanceof RepositoryNotFoundException) {
                        pull(workspace, true);
                    } else if (e.getCause() instanceof RefNotAdvertisedException) {
                        throw new UserMessage("notExistingBranch", workspace);
                    } else if (trial == 1 && e.getCause() instanceof TransportException) {
                        // Dieser Fehler kann auch bei anderen Git Aktionen auftreten und müsste dort
                        // ebenso behandelt werden!
                        // TODO Ich hab hier das Problem, dass ich das nicht reproduzieren kann.
                        // Fehlertext vom e.cause noch genauer untersuchen! Muss wohl irgendwas mit
                        // "not authenticated" sein.
                    	/* org.eclipse.jgit.api.errors.TransportException: http://gitlab-ci-token@gitlab01/-user-/manual: not authorized
        				at org.eclipse.jgit.api.FetchCommand.call(FetchCommand.java:249)
        				at org.eclipse.jgit.api.PullCommand.call(PullCommand.java:266)
        				at minerva.persistence.gitlab.git.GitService.pull(GitService.java:101)*/
                        new GitlabAuthService().refreshToken(user);
                        continue;
                    }
                    return;
                }
            }
        } else {
            workspaceFolder.mkdirs();
            String url = gitlab.getUrl() + "/" + project;
            Logger.info((forceClone ? "force " : "") + "clone from " + url
                    + " into " + workspaceFolder.getAbsolutePath());
            git.clone(url, user, branch, false);
        }
    }
    
    public void push(CommitMessage commitMessage, WorkspaceSO workspace,
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
        return git(workspace).getBranchNames();
    }
    
    public String getCommitHashOfHead(WorkspaceSO workspace) {
        return git(workspace).getCurrentCommitHash();
    }
    
    public List<ICommit> getFileHistory(String filename, boolean followRenames, WorkspaceSO workspace) {
        return git(workspace).getFileHistory(filename, followRenames);
    }

    public List<ICommit> getHtmlChangesHistory(WorkspaceSO workspace, int start, int size) {
        return git(workspace).getHtmlChangesHistory(start, size);
    }
    
    public void createBranch(WorkspaceSO workspace, String newBranch, String commit, User user) {
        GitService git = git(workspace);
        git.fetch(user);
        git.branch(newBranch, commit, user);
    }

    private GitService git(WorkspaceSO workspace) {
        return new GitService(new File(workspace.getFolder()));
    }
}
