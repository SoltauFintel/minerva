package minerva.persistence.gitlab;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.config.BackendService;
import minerva.config.ICommit;
import minerva.config.MinervaConfig;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.model.WorkspacesSO;
import minerva.persistence.gitlab.git.GitService;
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
			Logger.info(workspace.getUser().getLogin() + " | " + workspace.getBranch() + " | There are remote updates. -> pull"); // XXX DEBUG
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
            Set<String> removeFilenames) {
        repo.push(commitMessage, workspace, addFilenames, removeFilenames, () -> {});
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
    public List<ICommit> getSeiteMetaHistory(SeiteSO seite, boolean followRenames) {
        return repo.getFileHistory(seite.gitFilenameMeta(), followRenames, seite.getBook().getWorkspace());
    }

    @Override
    public List<ICommit> getHtmlChangesHistory(WorkspaceSO workspace, int start, int size) {
        return repo.getHtmlChangesHistory(workspace, start, size);
    }

    @Override
    public String logout(User user) {
        if (GitFactory.logout(user)) {
            return "Gitlab revoke ok";
        }
        return "";
    }

    @Override
    public void saveAll(CommitMessage commitMessage, WorkspaceSO workspace) {
        Set<String> add = new TreeSet<>();
        add.add(GitService.ADD_ALL_FILES);
        repo.push(commitMessage, workspace, add, new TreeSet<>(), () -> {});
    }

    @Override
    public void checkIfMoveIsAllowed(WorkspaceSO workspace) {
        if (workspace.getUser().getUserSettings().getDelayedPush().contains(workspace.getBranch())) {
            // User will loose Git history. So better end f-s mode before moving a page.
            throw new UserMessage("moveNotAllowedForFSMode", workspace);
        }
    }
}
