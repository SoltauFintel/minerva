package minerva.persistence.gitlab;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gitlab4j.api.GitLabApiException;
import org.pmw.tinylog.Logger;

import minerva.access.AbstractDirAccess;
import minerva.access.CommitHash;
import minerva.access.CommitMessage;
import minerva.model.GitlabRepositorySO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.seite.move.IMoveFile;
import minerva.seite.move.MoveFile;

public class GitlabDirAccess extends AbstractDirAccess {
    private final GitlabRepositorySO repo;

    public GitlabDirAccess(GitlabRepositorySO repo) {
        this.repo = repo;
    }
    
    @Override
    public void initWorkspace(WorkspaceSO workspace, boolean forceClone) {
        repo.pull(workspace, forceClone);
    }

    @Override
    public void saveFiles(Map<String, String> files, CommitMessage commitMessage, WorkspaceSO workspace) {
        repo.push(commitMessage, workspace, files.keySet(), emptySet(),
                () -> super.saveFiles(files, commitMessage, workspace));
    }

    @Override
    public void deleteFiles(Set<String> filenames, CommitMessage commitMessage, WorkspaceSO workspace,
            List<String> cantBeDeleted) {
        repo.push(commitMessage, workspace, emptySet(), filenames,
                () -> super.deleteFiles(filenames, commitMessage, workspace, cantBeDeleted));
    }
    
    @Override
    public void moveFiles(List<IMoveFile> files, CommitMessage commitMessage, WorkspaceSO workspace) {
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
    public void createBranch(WorkspaceSO workspace, String newBranch, String commit) {
        repo.createBranch(workspace, newBranch, commit, (GitlabUser) workspace.getUser().getUser());
    }
    
    @Override
    public List<String> getBranchNames(WorkspaceSO workspace) {
        return repo.getBranches(workspace);
    }

    @Override
    public void mergeBranch(String sourceBranch, String targetBranch, UserSO user) {
        try {
            new MergeRequestService().createAndMerge(
                    new CommitMessage("Merge " + sourceBranch + " into " + targetBranch).toString(),
                    sourceBranch,
                    targetBranch,
                    repo.getGitlabSystemUrl(), repo.getProject(),
                    (GitlabUser) user.getUser(),
                    user.getGuiLanguage());
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public CommitHash getCommitHash(WorkspaceSO workspace) {
        try {
            return new CommitHash(repo.getCommitHashOfHead(workspace));
        } catch (Exception e) {
            Logger.error("Can't load hash of HEAD commit.");
            return new CommitHash();
        }
    }
}
