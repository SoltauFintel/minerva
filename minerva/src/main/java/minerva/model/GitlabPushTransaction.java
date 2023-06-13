package minerva.model;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import minerva.git.GitService;
import minerva.git.MinervaEmptyCommitException;
import minerva.persistence.gitlab.GitlabUser;
import minerva.persistence.gitlab.MergeRequestService;

/**
 * Sub class for GitlabRepositorySO.
 * Order of method calls:
 * createWorkBranch -> switchToWorkBranch -> commitAndPush -> doMergeRequest -> finish.
 */
public class GitlabPushTransaction {
    private final GitlabRepositorySO repo;
    private final String commitMessage;
    private final WorkspaceSO workspace;
    private String workBranch;
    private GitService git;
    private GitlabUser user;
    private boolean doPull = false;
    
    public GitlabPushTransaction(GitlabRepositorySO repo, String commitMessage, WorkspaceSO workspace) {
        this.repo = repo;
        this.commitMessage = commitMessage;
        this.workspace = workspace;
    }
    
    public void createWorkBranch() {
        workBranch = WorkspacesSO.MINERVA_BRANCH
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("-yyyy-MM-dd-HHmmss-"))
                + workspace.getUser().getUser().getLogin();
        git = new GitService(new File(workspace.getFolder()));
        git.branch(workBranch);
    }
    
    public void switchToWorkBranch() {
        git.switchToBranch(workBranch);
    }
    
    /**
     * @param addFilenames files that have changed or are new
     * @param removeFilenames files to delete
     * @return true wenn erfolgreich
     */
    public boolean commitAndPush(Set<String> addFilenames, Set<String> removeFilenames) {
        try {
            user = (GitlabUser) workspace.getUser().getUser();
            String x = workspace.getFolder() + "/";
            Set<String> filesToAdd = addFilenames.stream().map(dn -> dn.replace(x, "")).collect(Collectors.toSet());
            Set<String> filesToRemove = removeFilenames.stream().map(dn -> dn.replace(x, "")).collect(Collectors.toSet());
            git.commit(commitMessage, user.getRealName(), user.getMail(), user, filesToAdd,
                    filesToRemove);
        } catch (MinervaEmptyCommitException ex) {
            Logger.info("no changes -> no commit and no merge request needed\nadd: "
                    + addFilenames + "\nremove: " + removeFilenames);
            return false;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    public void doMergeRequest() {
        try {
            new MergeRequestService().createAndSquashMergeRequest(commitMessage,
                    workBranch,
                    workspace.getBranch(),
                    repo.getGitlabSystemUrl(), repo.getProject(),
                    user);
            doPull = true;
        } catch (GitLabApiException e) {
            throw new RuntimeException( //
                    "Fehler beim Speichern: Merge Request kann nicht erstellt oder gemerget werden." + //
                    "\nArbeitsbranch: " + workBranch + ", Zielbranch: " + workspace.getBranch() + //
                    "\nStatus: " + e.getHttpStatus() + //
                    "\nDetails: " + e.getMessage(), e);
        }
    }
    
    public void finish() {
        git.switchToBranch(workspace.getBranch());
        if (doPull) {
            repo.pull(workspace, false);
        }
    }
}
