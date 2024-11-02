package gitper.persistence.gitlab;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.pmw.tinylog.Logger;

import gitper.BackendService;
import gitper.GitlabConfig;
import gitper.Gitper;
import gitper.User;
import gitper.Workspace;
import gitper.Workspaces;
import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import gitper.base.ICommit;
import gitper.base.StringService;
import gitper.persistence.gitlab.git.GitService;

public class GitlabBackendService implements BackendService {
    private final GitlabConfig config;
    private final GitlabSystem gitlabSystem;
    private final GitlabRepository repo;

    public GitlabBackendService(GitlabConfig config) {
        this.config = config;
        gitlabSystem = new GitlabSystem(config.getGitlabUrl());
        repo = new GitlabRepository(gitlabSystem, config.getGitlabProject());
    }

    @Override
    public String getInfo(String lang) {
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
    public User login(String login, String password, String pMail) {
        if (StringService.isNullOrEmpty(login) || StringService.isNullOrEmpty(password)) {
            return null;
        }
		Logger.debug(login + " | GitlabBackendService.login");
        User user = Gitper.gitperInterface.createUser(login);
        GitlabDataStore xu = new GitlabDataStore(user);
        xu.setPassword(password);
        String mail = gitlabSystem.login(user);
        if (mail == null) {
            mail = pMail;
        }
        if (mail == null) {
            return null;
        }
        user.setMailAddress(mail);
        return user;
    }
    
    @Override
    public String getUserFolder(User user) {
        String folder = user.getLogin();
        Logger.debug(user.getLogin() + " | folder: " + folder);
        return folder;
    }

    @Override
    public void uptodatecheck(Workspace workspace, UpdateAction updateAction) {
        File workspaceFolder = new File(workspace.getFolder());
        User user = workspace.user();
        boolean areThereRemoteUpdates = new GitService(workspaceFolder).areThereRemoteUpdates(workspace.getBranch(), user);
        if (areThereRemoteUpdates) {
            workspace.pull();
            updateAction.update();
        }
    }

    @Override
    public List<String> getAddableBranches(Workspaces workspaces, Workspace ref) {
        ref.pull();
        List<String> ret = repo.getBranches(ref);
        ret.removeIf(branch -> branch.toLowerCase().contains(GitlabPushTransaction.MINERVA_BRANCH));
        ret.removeAll(workspaces.getBranches());
        return ret;
    }

    @Override
    public void saveFiles(CommitMessage commitMessage, Workspace workspace, Set<String> addFilenames,
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
    public List<ICommit> getHtmlChangesHistory(Workspace workspace, int start, int size) {
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
    public void saveAll(CommitMessage commitMessage, Workspace workspace) {
        Set<String> add = new TreeSet<>();
        add.add(GitService.ADD_ALL_FILES);
        repo.push(commitMessage, workspace, add, new TreeSet<>(), () -> {});
    }
    
    @Override
    public void checkIfMoveIsAllowed(Workspace workspace) {
        if (workspace.user().getDelayedPush().contains(workspace.getBranch())) {
            // User will loose Git history. So better end f-s mode before moving a page.
            throw new MoveNotAllowedForFSModeException(workspace);
        }
    }
    
    @Override
    public Object forceReloadIfCheap(String filenameMeta) {
        return null; // It's expensive. Do not update page.
    }

    @Override
    public List<ICommit> getFileHistory(String filename, Workspace workspace, boolean followRenames) {
        return getHistory(filename, workspace, followRenames);
    }
    
    protected final List<ICommit> getHistory(String filename, Workspace workspace, boolean followRenames) {
        return repo.getFileHistory(filename, followRenames, workspace);
    }
}
