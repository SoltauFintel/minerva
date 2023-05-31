package minerva.seite;

import minerva.base.NlsString;

public class PostContentsData {
    private final String branch;
    private final String bookFolder;
    /** Seite ID */
    private final String id;
    private final int version;
    private final NlsString content = new NlsString();
    private PostContentsData parent;
    private boolean done = false;
    
    public PostContentsData(String branch, String bookFolder, String id, int version) {
        this.branch = branch;
        this.bookFolder = bookFolder;
        this.id = id;
        this.version = version;
    }

    public String getBranch() {
        return branch;
    }

    public String getBookFolder() {
        return bookFolder;
    }

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public NlsString getContent() {
        return content;
    }

    public PostContentsData getParent() {
        return parent;
    }

    public void setParent(PostContentsData parent) {
        this.parent = parent;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
