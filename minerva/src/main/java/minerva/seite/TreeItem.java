package minerva.seite;

import java.util.List;
import java.util.Set;

import github.soltaufintel.amalia.web.action.Escaper;

public class TreeItem {
    private final String id;
    private final String title;
    private final int hasContent;
    private final String bookFolder;
    private final String branch;
    private final boolean current;
    private final TreeItem parent;
    private List<TreeItem> subitems;
    private boolean expanded = false;
    private final Set<String> tags;

    public TreeItem(String id, String title, Set<String> tags, boolean current, int hasContent, String branch, String bookFolder, TreeItem parent) {
        this.id = id;
        this.title = title;
        this.hasContent = hasContent;
        this.bookFolder = bookFolder;
        this.branch = branch;
        this.parent = parent;
        this.current = current;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int hasContent() {
        return hasContent;
    }

    public boolean isCurrent() {
        return current;
    }

    public TreeItem getParent() {
        return parent;
    }

    public List<TreeItem> getSubitems() {
        return subitems;
    }

    public void setSubitems(List<TreeItem> subitems) {
        this.subitems = subitems;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    public String getLink() {
        return "/s/" + Escaper.esc(branch) + "/" + Escaper.esc(bookFolder) + "/" + Escaper.esc(id);
    }

	public Set<String> getTags() {
		return tags;
	}
}
