package gitper.persistence.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import gitper.BackendService;
import gitper.Gitper;
import gitper.User;
import gitper.Workspace;
import gitper.Workspaces;
import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import gitper.base.ICommit;
import gitper.base.StringService;

public abstract class FileSystemBackendService implements BackendService {
    private final MinervaFileSystemConfig fsa;
    
    public FileSystemBackendService(MinervaFileSystemConfig fsa) {
        this.fsa = fsa;
    }

    @Override
    public String getInfo(String lang) {
        return fsa.n(lang, "filesystem") + " (" + new File(fsa.getWorkspacesFolder()).getAbsolutePath() + ")";
    }

    @Override
    public DirAccess getDirAccess() {
        return new FileSystemDirAccess();
    }

    @Override
    public boolean withPassword() {
        return false;
    }

    @Override
    public User login(String login, String password, String mail) {
        if (StringService.isNullOrEmpty(login)) {
            return null;
        }
        if (!fsa.getEditorPassword().equals(password)) {
            return null;
        }
        return Gitper.gitperInterface.loadUser(login, true, mail);
    }
    
    @Override
    public String getUserFolder(User user) {
        String folder = fsa.getUserFolder();
        Logger.debug(user.getLogin() + " | folder: " + folder);
        return folder;
    }

    @Override
    public void uptodatecheck(Workspace workspace, UpdateAction updateAction) { //
    }

    @Override
    public List<String> getAddableBranches(Workspaces workspaces, Workspace ref) {
        return new ArrayList<>();
    }

    @Override
    public void saveFiles(CommitMessage commitMessage, Workspace workspace, Set<String> addFilenames, Set<String> removeFilenames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMergeRequestPath(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCommitLink(String hash) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ICommit> getFileHistory(String filename, Workspace workspace, boolean followRenames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ICommit> getHtmlChangesHistory(Workspace workspace, int start, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String logout(User user) {
        return "";
    }

    @Override
    public void saveAll(CommitMessage commitMessage, Workspace workspace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkIfMoveIsAllowed(Workspace workspace) { // it's always allowed
    }
    
	public interface MinervaFileSystemConfig {

		String getWorkspacesFolder();

		String getEditorPassword();

		String getUserFolder();

		String n(String lang, String key);
	}
}
