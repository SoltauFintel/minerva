package minerva.persistence.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.StringService;
import minerva.config.BackendService;
import minerva.config.ICommit;
import minerva.config.MinervaConfig;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.model.WorkspacesSO;
import minerva.seite.Seite;
import minerva.user.User;

public class FileSystemBackendService implements BackendService {
    private final MinervaConfig config;
        
    public FileSystemBackendService(MinervaConfig config) {
        this.config = config;
    }

    @Override
    public String getInfo() {
        return "Dateisystem (" + new File(config.getWorkspacesFolder()).getAbsolutePath() + ")";
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
    public User login(String login, String password) {
        if (StringService.isNullOrEmpty(login)) {
            return null;
        }
        if (!MinervaWebapp.factory().getConfig().getEditorPassword().equals(password)) {
            return null;
        }
        return loginUser(login);
    }
    
    public static User loginUser(String login) {
        String folder = MinervaWebapp.factory().getConfig().getUserFolder();
        if (folder.isEmpty()) {
            folder = login;
        }
        Logger.debug(login + " | folder: " + folder);
        return new User(login, folder);
    }

    @Override
    public void uptodatecheck(WorkspaceSO workspace, UpdateAction updateAction) { //
    }

    @Override
    public Seite forceReloadIfCheap(String filenameMeta) {
        return new MultiPurposeDirAccess(getDirAccess()).load(filenameMeta, Seite.class);
    }

    @Override
    public List<String> getAddableBranches(WorkspacesSO workspaces, WorkspaceSO ref) {
        return new ArrayList<>();
    }

    @Override
    public void saveFiles(CommitMessage commitMessage, WorkspaceSO workspace, Set<String> addFilenames, Set<String> removeFilenames) {
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
    public List<ICommit> getSeiteMetaHistory(SeiteSO seite, boolean followRenames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ICommit> getHtmlChangesHistory(WorkspaceSO workspace, int start, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String logout(User user) {
        return "";
    }
}
