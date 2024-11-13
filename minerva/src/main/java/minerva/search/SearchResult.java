package minerva.search;

import java.util.ArrayList;
import java.util.List;

import minerva.seite.Breadcrumb;

public class SearchResult {
    private String path;
    private String title;
    private String content;
    private final List<Breadcrumb> breadcrumbs = new ArrayList<>();
    private String featureNumber = null;

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

	public String getFeatureNumber() {
		return featureNumber;
	}

	public void setFeatureNumber(String featureNumber) {
		this.featureNumber = featureNumber;
	}
}
