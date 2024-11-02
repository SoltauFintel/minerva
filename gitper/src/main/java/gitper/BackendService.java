package gitper;

import java.util.List;
import java.util.Set;

import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import gitper.base.ICommit;

public interface BackendService {

    /**
     * @return password for login needed?
     */
    boolean withPassword();
    
    /**
     * @param login -
     * @param password -
     * @param emailAddress -
     * @return null if login failed
     */
    User login(String login, String password, String emailAddress);
    
    /**
     * @param user -
     * @return additional logout info
     */
    String logout(User user);

    /**
     * @param user -
     * @return user folder
     */
    String getUserFolder(User user);

    /**
     * @param lang language
     * @return name of backend and details
     */
    String getInfo(String lang);
    
    /**
     * @return backend-specific persistence service
     */
    DirAccess getDirAccess();
    
    /**
     * @param filenameMeta -
     * @return null for not updating page, else: Seite
     */
    Object forceReloadIfCheap(String filenameMeta);

    List<String> getAddableBranches(Workspaces workspaces, Workspace ref);
    
    void saveFiles(CommitMessage commitMessage, Workspace workspace, Set<String> addFilenames, Set<String> removeFilenames);
    
    void saveAll(CommitMessage commitMessage, Workspace workspace);
    
    void checkIfMoveIsAllowed(Workspace workspace);
    
    String getMergeRequestPath(Long id);
    
    String getCommitLink(String hash);

    List<ICommit> getFileHistory(String filename, Workspace workspace, boolean followRenames);

    List<ICommit> getHtmlChangesHistory(Workspace workspace, int start, int size);
    
    void uptodatecheck(Workspace workspace, UpdateAction updateAction);
    
    public interface UpdateAction {
        
        /**
         * Execute some code after workspace has been pulled.
         */
        void update();
    }
}
