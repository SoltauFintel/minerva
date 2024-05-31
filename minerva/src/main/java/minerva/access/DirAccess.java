package minerva.access;

import java.util.List;
import java.util.Map;
import java.util.Set;

import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.seite.move.IMoveFile;

/**
 * That's an generic DAO.
 */
public interface DirAccess {
    /** Indicator that entry is an image. */
    String IMAGE = "$$image";
    
    /**
     * Load or update workspace if needed
     * @param workspace -
     * @param force false: update (or load if needed), true: load in new folder
     */
    void initWorkspace(WorkspaceSO workspace, boolean force);
    
    /**
     * alle Workspaces ermitteln
     * @param folder parent folder
     * @return folder names, without parent path
     */
    List<String> getAllFolders(String folder);

    /**
     * Load all files with contents from given folder.
     * @param folder relative filename in workspace
     * @return map with key=filename, value=content
     */
    Map<String,String> loadAllFiles(String folder);

    Map<String,String> loadAllFiles(String folder, String postfix);
    
    /**
     * Load many plain text files
     * @param filenames relative filenames in workspace
     * @return map with key=filename, value=content. null as content if file does not exist.
     */
    Map<String,String> loadFiles(Set<String> filenames);

    /**
     * @param files map with key=filename, value=content. null as content deletes file.
     * @param commitMessage -
     * @param workspace -
     */
    void saveFiles(Map<String, String> files, CommitMessage commitMessage, WorkspaceSO workspace);

    /**
     * @param filenames zu löschende Ordner (Eintrag endet mit '/*') oder Dateien
     * @param commitMessage -
     * @param workspace -
     * @param cantBeDeleted not null, nach dem Aufruf sind dadrin die Dateien, die nicht gelöscht werden konnten
     */
    void deleteFiles(Set<String> filenames, CommitMessage commitMessage, WorkspaceSO workspace, List<String> cantBeDeleted);

    /**
     * @param files changed or moved files
     * @param commitMessage -
     * @param workspace -
     */
    void moveFiles(List<IMoveFile> files, CommitMessage commitMessage, WorkspaceSO workspace);

    /**
     * Create and push new branch. Does not switch to the branch.
     * @param workspace workspace of old branch
     * @param newBranch name of new branch
     * @param commit place where to branch, commit hash or tag in old branch, can be null
     */
    void createBranch(WorkspaceSO workspace, String newBranch, String commit);
    
    /**
     * @param workspace -
     * @return all branch names
     */
    List<String> getBranchNames(WorkspaceSO workspace);

    /**
     * Merge source branch into target branch.
     * @param sourceBranch branch name, e.g. "feature-4711"
     * @param targetBranch branch name, often "master"
     * @param user current user
     */
    void mergeBranch(String sourceBranch, String targetBranch, UserSO user);
    
    /**
     * @param workspace -
     * @return CommitHash of given workspace, not null
     */
    CommitHash getCommitHash(WorkspaceSO workspace);
    
    /**
     * Copy binary files from source folder to target folder
     * @param bookFolder -
     * @param source -
     * @param target -
     * @return list of target filenames with short path
     */
    List<String> copyFiles(String bookFolder, String source, String target);
}
