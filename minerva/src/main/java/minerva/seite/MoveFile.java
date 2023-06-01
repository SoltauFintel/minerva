package minerva.seite;

public class MoveFile {
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
