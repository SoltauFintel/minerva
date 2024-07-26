package minerva.migration.confluence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ConfluencePage {
    private String id;
    private int position;
    private String title;
    private Set<String> labels = new TreeSet<>();
    private List<ConfluencePage> subpages = new ArrayList<>();
    private boolean empty;
    private List<ConfluenceAncestor> ancestors = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public List<ConfluencePage> getSubpages() {
        return subpages;
    }

    public void setSubpages(List<ConfluencePage> subpages) {
        this.subpages = subpages;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public List<ConfluenceAncestor> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<ConfluenceAncestor> ancestors) {
        this.ancestors = ancestors;
    }
}
