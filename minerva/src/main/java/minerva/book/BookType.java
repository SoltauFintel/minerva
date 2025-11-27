package minerva.book;

public enum BookType {

    PUBLIC,
    RELEASE_NOTES,
    INTERNAL,
    FEATURE_TREE;
    
    public boolean isPublic() {
        return PUBLIC.equals(this) || RELEASE_NOTES.equals(this);
    }
    
    public boolean isInternal() {
        return INTERNAL.equals(this) || FEATURE_TREE.equals(this);
    }
}
