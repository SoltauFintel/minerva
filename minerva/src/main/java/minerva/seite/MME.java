package minerva.seite;

/** MindMap entry */
public class MME {
    public String id;
    public String parentId;
    public String text;
    public String type;

    public MME(String id, MME parent, String text, String type) {
        this.id = id;
        this.parentId = parent == null ? null : parent.id;
        this.text = text;
        this.type = type;
    }

    public MME(String id, MME parent, String text) {
        this(id, parent, text, null);
    }

    /** root */
    public MME(String text, String type) {
        id = null;
        parentId = null;
        this.text = text;
        this.type = type;
    }
}
