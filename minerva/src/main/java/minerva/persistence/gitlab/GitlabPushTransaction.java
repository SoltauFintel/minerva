package minerva.persistence.gitlab;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.model.WorkspaceSO;
import minerva.model.WorkspacesSO;
import minerva.persistence.gitlab.git.GitService;
import minerva.persistence.gitlab.git.MinervaEmptyCommitException;

/**
 * Sub class for GitlabRepositorySO.
 * Order of method calls:
 * createWorkBranch -> switchToWorkBranch -> commitAndPush -> doMergeRequest -> finish.
 */
public class GitlabPushTransaction {
    private final GitlabRepository repo;
    private final CommitMessage commitMessage;
    private final WorkspaceSO workspace;
    private String workBranch;
    private GitService git;
    private GitlabUser user;
    private boolean doPull = false;
    
    public GitlabPushTransaction(GitlabRepository repo, CommitMessage commitMessage, WorkspaceSO workspace) {
        if (commitMessage == null) {
            throw new IllegalArgumentException("commitMessage must not be null");
        }
        this.repo = repo;
        this.commitMessage = commitMessage;
        this.workspace = workspace;
    }
    
    public void createWorkBranch() {
        workBranch = WorkspacesSO.MINERVA_BRANCH
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("-yyyy-MM-dd-HHmmss-"))
                + workspace.getUser().getLogin();
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
            workspace.onPush();
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
            MergeRequestService mrs = new MergeRequestService();
            if (commitMessage.isBigCommit()) {
                mrs.waitLonger();
            }
            mrs.createAndSquash(commitMessage.toString(),
                    workBranch,
                    workspace.getBranch(),
                    repo.getGitlabSystemUrl(), repo.getProject(),
                    user,
                    user.getGuiLanguage());
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
