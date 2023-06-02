package minerva.migration;

/**
 * English page without German page
 */
public class EnglishSoloPage {
    private final String id;
    /** empty: no parent page specified */
    private final String parentId;

    public EnglishSoloPage(String id, String parentId) {
        this.id = id;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }
}
