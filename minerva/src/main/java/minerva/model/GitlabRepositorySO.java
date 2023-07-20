package minerva.model;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.base.UserMessage;
import minerva.git.CommitMessage;
import minerva.git.GitService;
import minerva.git.HCommit;
import minerva.persistence.gitlab.GitlabAuthService;
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
                        // TODO Dieser Fehler kann auch bei anderen Git Aktionen auftreten und müsste dort
                        //      ebenso behandelt werden!                        
                        // TODO Fehlertext vom e.cause noch genauer untersuchen! Muss wohl irgendwas mit
                        //      "not authenticated" sein.
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
    
    public List<HCommit> getSeiteMetaHistory(SeiteSO seite, boolean followRenames) {
        return git(seite.getBook().getWorkspace()).getHistory(seite.gitFilenameMeta(), followRenames);
    }
    
    public void createBranch(WorkspaceSO workspace, String newBranch, String commit, GitlabUser user) {
        GitService git = git(workspace);
        git.fetch(user);
        git.branch(newBranch, commit, user);
    }

    private GitService git(WorkspaceSO workspace) {
        return new GitService(new File(workspace.getFolder()));
    }
}
