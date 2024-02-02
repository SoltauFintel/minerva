package minerva.seite;

import minerva.base.NlsString;

public class PostContentsData implements IPostContentsData {
    private final String branch;
    private final String bookFolder;
    /** Seite ID */
    private final String id;
    private final int version;
    private final NlsString content = new NlsString();
    private final NlsString title = new NlsString();
    private final String comment;
    private PostContentsData parent;
    private boolean done = false;
    
    public PostContentsData(String branch, String bookFolder, String id, String comment, int version) {
        this.branch = branch;
        this.bookFolder = bookFolder;
        this.id = id;
        this.comment = comment == null ? "" : comment.trim();
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

    @Override
    public String getComment() {
        return comment;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public NlsString getContent() {
        return content;
    }

    @Override
    public NlsString getTitle() {
        return title;
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

    @Override
    public void setDone(boolean done) {
        this.done = done;
    }
}
