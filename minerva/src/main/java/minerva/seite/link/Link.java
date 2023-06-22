package minerva.seite.link;

public class Link {
    /** href attribute of the a tag */
    private String href;
    /** Plain text between the starting and the closing a tag, can be empty */
    private String title = "";
    /** new href value (page ID), null if user hasn't decided a value yet */
    private String seiteId;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return not null, can be empty
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) {
            this.title = "";
        } else {
            this.title = title;
        }
    }

    public String getSeiteId() {
        return seiteId;
    }

    public void setSeiteId(String seiteId) {
        this.seiteId = seiteId;
    }
}
