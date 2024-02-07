package minerva.postcontents;

public abstract class PostContentsData {
    private final String key;
    private final int version;
    private PostContentsData previous; // einfach verkettete Liste
    
    public PostContentsData(String key, int version) {
        this.key = key;
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public int getVersion() {
        return version;
    }

    public PostContentsData getPrevious() {
        return previous;
    }

    public void setPrevious(PostContentsData previous) {
        this.previous = previous;
    }
}
