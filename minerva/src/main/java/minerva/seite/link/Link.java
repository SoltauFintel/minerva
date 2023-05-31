package minerva.seite.link;

public class Link {
    /** href Attribut des a tags */
    private String href;
    /** Text zwischen den a tags */
    private String title;
    /** neuer href Wert, null wenn Benutzer noch keinen entschieden hat */
    private String seiteId;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeiteId() {
        return seiteId;
    }

    public void setSeiteId(String seiteId) {
        this.seiteId = seiteId;
    }
}
