package minerva.access;

/**
 * ID for commit
 */
public class CommitHash {
    private final String hash;

    /**
     * unknown commit hash
     */
    public CommitHash() {
        this("");
    }

    public CommitHash(String hash) {
        this.hash = hash == null ? "" : hash;
    }

    /**
     * @return can be empty, not null
     */
    public String getHash() {
        return hash;
    }
    
    /**
     * @return can be empty, not null
     */
    public String getShortHash() {
        return hash.length() > 7 ? hash.substring(0, 7) : hash;
    }
}
