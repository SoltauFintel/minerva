package gitper.persistence.gitlab;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import gitper.User;
import gitper.Workspace;
import gitper.access.CommitMessage;
import gitper.base.StringService;
import gitper.persistence.gitlab.git.GitService;
import gitper.persistence.gitlab.git.MinervaEmptyCommitException;

/**
 * Sub class for GitlabRepositorySO.
 * Order of method calls:
 * createWorkBranch -> switchToWorkBranch -> commitAndPush -> doMergeRequest -> finish.
 */
public class GitlabPushTransaction {
    public static final String MINERVA_BRANCH = "minerva";
    private final GitlabRepository repo;
    private final CommitMessage commitMessage;
    private final Workspace workspace;
    private String workBranch;
    private GitService git;
    private User user;
    private boolean doPull = false;
    
    public GitlabPushTransaction(GitlabRepository repo, CommitMessage commitMessage, Workspace workspace) {
        if (commitMessage == null) {
            throw new IllegalArgumentException("commitMessage must not be null");
        }
        this.repo = repo;
        this.commitMessage = commitMessage;
        this.workspace = workspace;
    }
    
    public void createWorkBranch() {
        workBranch = MINERVA_BRANCH
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("-yyyy-MM-dd-HHmmss-"))
                + workspace.user().getLogin();
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
            user = workspace.user();
            String x = workspace.getFolder() + "/";
            Set<String> filesToAdd = addFilenames.stream().map(dn -> dn.replace(x, "")).collect(Collectors.toSet());
            Set<String> filesToRemove = removeFilenames.stream().map(dn -> dn.replace(x, "")).collect(Collectors.toSet());
            String name = StringService.isNullOrEmpty(user.getRealName()) ? user.getLogin() : user.getRealName();
            git.commit(commitMessage, name, user.getMailAddress(), user, filesToAdd, filesToRemove);
            workspace.onPush();
        } catch (MinervaEmptyCommitException ex) {
            checkIfExist(addFilenames, "addFilenames");
            checkIfExist(removeFilenames, "removeFilenames");
            if (addFilenames.size() > 5 || removeFilenames.size() > 5) { // prevent ultra long log message output
                Logger.info("no changes -> no commit and no merge request needed. add: "
                        + addFilenames.size() + ", remove: " + removeFilenames.size());
            } else {
                Logger.info("no changes -> no commit and no merge request needed\nadd: "
                        + addFilenames + "\nremove: " + removeFilenames);
            }
            return false;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }
    
    private void checkIfExist(Set<String> filenames, String name) {
        if (!filenames.isEmpty()) {
            String dn = filenames.iterator().next();
            File file = new File(dn);
            if (!file.exists()) {
                Logger.info("[" + name + "] Check filename! File does not exist: " + file.getAbsolutePath());
            }
        }
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
