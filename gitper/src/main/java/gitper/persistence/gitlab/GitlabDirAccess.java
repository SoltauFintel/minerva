package gitper.persistence.gitlab;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import gitper.User;
import gitper.Workspace;
import gitper.access.AbstractDirAccess;
import gitper.access.CommitHash;
import gitper.access.CommitMessage;
import gitper.movefile.IMoveFile;
import gitper.movefile.MoveFile;

public class GitlabDirAccess extends AbstractDirAccess {
    private final GitlabRepository repo;

    public GitlabDirAccess(GitlabRepository repo) {
        this.repo = repo;
    }
    
    @Override
    public void initWorkspace(Workspace workspace, boolean forceClone) {
        repo.pull(workspace, forceClone);
    }

    @Override
    public void saveFiles(Map<String, String> files, CommitMessage commitMessage, Workspace workspace) {
        repo.push(commitMessage, workspace, files.keySet(), emptySet(),
                () -> super.saveFiles(files, commitMessage, workspace));
    }

    @Override
    public void deleteFiles(Set<String> filenames, CommitMessage commitMessage, Workspace workspace,
            List<String> cantBeDeleted) {
        repo.push(commitMessage, workspace, emptySet(), filenames,
                () -> super.deleteFiles(filenames, commitMessage, workspace, cantBeDeleted));
    }
    
    @Override
    public void moveFiles(List<IMoveFile> files, CommitMessage commitMessage, Workspace workspace) {
        Set<String> add = new HashSet<>();
        Set<String> rm = new HashSet<>();
        for (IMoveFile f : files) {
            if (f instanceof MoveFile mf) {
                add.add(mf.getNewFile());
                rm.add(mf.getOldFile());
            }
        }
        repo.push(commitMessage, workspace, add, rm,
                () -> super.moveFiles(files, commitMessage, workspace));
    }

    private HashSet<String> emptySet() {
        return new HashSet<>();
    }

    @Override
    public void createBranch(Workspace workspace, String newBranch, String commit) {
        repo.createBranch(workspace, newBranch, commit, workspace.user());
    }
    
    @Override
    public List<String> getBranchNames(Workspace workspace) {
        return repo.getBranches(workspace);
    }

    @Override
    public void mergeBranch(String sourceBranch, String targetBranch, User user) {
        try {
            new MergeRequestService().createAndMerge(
                    new CommitMessage("Merge " + sourceBranch + " into " + targetBranch).toString(),
                    sourceBranch,
                    targetBranch,
                    repo.getGitlabSystemUrl(), repo.getProject(),
                    user);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public CommitHash getCommitHash(Workspace workspace) {
        try {
            return new CommitHash(repo.getCommitHashOfHead(workspace));
        } catch (Exception e) {
            Logger.error("Can't load hash of HEAD commit.");
            return new CommitHash();
        }
    }
}
