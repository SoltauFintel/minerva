package minerva.access;

import java.util.List;
import java.util.Map;
import java.util.Set;

import minerva.model.WorkspaceSO;
import minerva.seite.MoveFile;

/**
 * That's an generic DAO.
 */
public interface DirAccess {
    String IMAGE = "$$image";
    
    void initWorkspace(WorkspaceSO workspace, boolean force);
    
    List<String> getAllFolders(String folder);

    /**
     * Load all files with contents from given folder.
     * @param folder relative filename in workspace
     * @return map with key=filename, value=content
     */
    Map<String,String> loadAllFiles(String folder);
    
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
    void saveFiles(Map<String, String> files, String commitMessage, WorkspaceSO workspace);

    /**
     * @param filenames zu löschende Ordner (Eintrag endet mit '/*') oder Dateien
     * @param commitMessage -
     * @param workspace -
     * @param cantBeDeleted not null, nach dem Aufruf sind dadrin die Dateien, die nicht gelöscht werden konnten
     * @return Liste der Dateien, die nicht gelöscht werden konnten
     */
    void deleteFiles(Set<String> filenames, String commitMessage, WorkspaceSO workspace, List<String> cantBeDeleted);

    void moveFiles(List<MoveFile> files, String commitMessage, WorkspaceSO workspace);
}
