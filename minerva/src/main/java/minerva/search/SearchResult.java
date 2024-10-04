package minerva.search;

import java.util.ArrayList;
import java.util.List;

import minerva.seite.Breadcrumb;

public class SearchResult {
    private String path;
    private String title;
    private String content;
    private final List<Breadcrumb> breadcrumbs = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}
}
