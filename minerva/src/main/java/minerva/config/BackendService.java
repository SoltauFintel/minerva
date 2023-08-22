package minerva.config;

import java.util.List;
import java.util.Set;

import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.model.WorkspacesSO;
import minerva.seite.Seite;
import minerva.user.User;

public interface BackendService {

    /**
     * @return password for login needed?
     */
    boolean withPassword();
    
    /**
     * @param login -
     * @param password -
     * @return null if login failed
     */
    User login(String login, String password);
    
    /**
     * @param user -
     * @return additional logout info
     */
    String logout(User user);

    /**
     * @return name of backend and details
     */
    String getInfo();
    
    /**
     * @return backend-specific persistence service
     */
    DirAccess getDirAccess();
    
    /**
     * @param filenameMeta -
     * @return null for not updating page
     */
    Seite forceReloadIfCheap(String filenameMeta);

    List<String> getAddableBranches(WorkspacesSO workspaces, WorkspaceSO ref);
    
    void saveFiles(CommitMessage commitMessage, WorkspaceSO workspace, Set<String> addFilenames, Set<String> removeFilenames);
    
    String getMergeRequestPath(Long id);
    
    String getCommitLink(String hash);
    
    List<ICommit> getSeiteMetaHistory(SeiteSO seite, boolean followRenames);

    List<ICommit> getHtmlChangesHistory(WorkspaceSO workspace, int start, int size);
    
    void uptodatecheck(WorkspaceSO workspace, UpdateAction updateAction);
    
    public interface UpdateAction {
        
        /**
         * Execute some code after workspace has been pulled.
         */
        void update();
    }
}
