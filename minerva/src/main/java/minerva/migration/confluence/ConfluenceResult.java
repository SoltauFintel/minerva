package minerva.migration.confluence;

import java.util.List;

/** important class */
public class ConfluenceResult {
    private String id;
    private String title;
    private ConfluencePageBody body;
    private List<ConfluenceAncestor> ancestors;
    private ConfluenceExtensions extensions;
    
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

    public ConfluencePageBody getBody() {
        return body;
    }

    public void setBody(ConfluencePageBody body) {
        this.body = body;
    }

    public List<ConfluenceAncestor> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<ConfluenceAncestor> ancestors) {
        this.ancestors = ancestors;
    }

    public ConfluenceExtensions getExtensions() {
        return extensions;
    }

    public void setExtensions(ConfluenceExtensions extensions) {
        this.extensions = extensions;
    }
}
