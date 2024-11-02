package gitper.movefile;

public class ChangeFile implements IMoveFile {
    private final String filename;
    private final String content;

    public ChangeFile(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }
}
