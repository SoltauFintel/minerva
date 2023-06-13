package minerva.seite;

/**
 * Move file (without changing its content) or move whole folder
 */
public class MoveFile implements IMoveFile {
    private final String oldFile;
    private final String newFile;

    public MoveFile(String oldFile, String newFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
    }

    public String getOldFile() {
        return oldFile;
    }

    public String getNewFile() {
        return newFile;
    }
}
