package minerva.persistence.gitlab;

import java.io.File;
import java.util.List;
import java.util.Set;

import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.base.StringService;
import minerva.config.BackendService;
import minerva.config.MinervaConfig;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.model.WorkspacesSO;
import minerva.persistence.gitlab.GitlabRepository.SaveProcedure;
import minerva.persistence.gitlab.git.GitService;
import minerva.persistence.gitlab.git.HCommit;
import minerva.seite.Seite;
import minerva.user.User;

public class GitlabBackendService implements BackendService {
    private final MinervaConfig config;
    private final GitlabSystem gitlabSystem;
    private final GitlabRepository repo;

    public GitlabBackendService(MinervaConfig config) {
        this.config = config;
        gitlabSystem = new GitlabSystem(config.getGitlabUrl());
        repo = new GitlabRepository(gitlabSystem, config.getGitlabProject());
    }

    @Override
    public String getInfo() {
        return "Gitlab (" + repo.getProjectUrl() + ")";
    }

    @Override
    public DirAccess getDirAccess() {
        return new GitlabDirAccess(repo);
    }

    @Override
    public boolean withPassword() {
        return true;
    }

    @Override
    public User login(String login, String password) {
        if (StringService.isNullOrEmpty(login) || StringService.isNullOrEmpty(password)) {
            return null;
        }
        GitlabUser user = new GitlabUser(login, password);
        String mail = gitlabSystem.login(user);
        if (mail == null) {
            return null;
        }
        user.setMail(mail);
        return user;
    }

    @Override
    public void uptodatecheck(WorkspaceSO workspace, UpdateAction updateAction) {
        File workspaceFolder = new File(workspace.getFolder());
        GitlabUser gitlabUser = (GitlabUser) workspace.getUser().getUser();
        boolean areThereRemoteUpdates = new GitService(workspaceFolder).areThereRemoteUpdates(workspace.getBranch(), gitlabUser);
        if (areThereRemoteUpdates) {
            workspace.pull();
            updateAction.update();
        }
    }

    @Override
    public Seite forceReloadIfCheap(String filenameMeta) {
        return null; // It's expensive. Do not update page.
    }

    @Override
    public List<String> getAddableBranches(WorkspacesSO workspaces, WorkspaceSO ref) {
        ref.pull();
        List<String> ret = repo.getBranches(ref);
        ret.removeIf(branch -> branch.toLowerCase().contains(WorkspacesSO.MINERVA_BRANCH));
        for (WorkspaceSO w : workspaces) {
            ret.remove(w.getBranch());
        }
        return ret;
    }

    @Override
    public void saveFiles(CommitMessage commitMessage, WorkspaceSO workspace, Set<String> addFilenames,
            Set<String> removeFilenames, SaveProcedure saveFiles) {
        repo.push(commitMessage, workspace, addFilenames, removeFilenames, saveFiles);
    }

    @Override
    public String getMergeRequestPath(Long id) {
        return repo.getProjectUrl() + config.getGitlabMergeRequestPath()
            + id;
    }

    @Override
    public String getCommitLink(String hash) {
        return repo.getProjectUrl() + config.getGitlabCommitPath() // http://host:port/user/repo/-/commit/
            + hash;
    }

    @Override
    public List<HCommit> getSeiteMetaHistory(SeiteSO seite, boolean followRenames) {
        return repo.getFileHistory(seite.gitFilenameMeta(), followRenames, seite.getBook().getWorkspace());
    }

    @Override
    public List<HCommit> getHtmlChangesHistory(WorkspaceSO workspace, int start, int size) {
        return repo.getHtmlChangesHistory(workspace, start, size);
    }
}
