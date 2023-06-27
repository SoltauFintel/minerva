package minerva.publish;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Minerva PageData class
 */
public class TocEntry {
    private String id;
    private String title;
    private final Set<String> labels = new TreeSet<>();
    private final List<String> helpKeys = new ArrayList<>();
    private final List<TocEntry> subpages = new ArrayList<>();

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

    public Set<String> getLabels() {
        return labels;
    }
    
    public List<String> getHelpKeys() {
        return helpKeys;
    }

    public List<TocEntry> getSubpages() {
        return subpages;
    }
}
