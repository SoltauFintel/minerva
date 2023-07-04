package minerva.subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import minerva.base.NlsString;

public class TPage {
    private String id;
    private String parentId;
    private final NlsString title = new NlsString();
    private int position;
    private boolean sorted = true;
    private final Set<String> tags = new TreeSet<>();
    private final List<String> helpKeys = new ArrayList<>();
    private Map<String, String> html;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isSorted() {
        return sorted;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    public Map<String, String> getHtml() {
        return html;
    }

    public void setHtml(Map<String, String> html) {
        this.html = html;
    }

    public NlsString getTitle() {
        return title;
    }

    public Set<String> getTags() {
        return tags;
    }

    public List<String> getHelpKeys() {
        return helpKeys;
    }
}
