package minerva.migration.confluence;

import java.util.ArrayList;
import java.util.List;

public class ConfluencePage2 {
    private String id;
    private String title;
    private String parentId;
    private final List<ConfluencePage2> subpages = new ArrayList<>();
    private transient Integer position;
    private transient String html;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<ConfluencePage2> getSubpages() {
        return subpages;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
