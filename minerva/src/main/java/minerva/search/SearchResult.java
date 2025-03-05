package minerva.search;

import java.util.ArrayList;
import java.util.List;

import gitper.base.StringService;
import minerva.seite.Breadcrumb;

public class SearchResult {
    private String path;
    private String title;
    private String content;
    private final List<Breadcrumb> breadcrumbs = new ArrayList<>();
    private String featureNumber = null;
	private int hits;
	/** book folder, table, BO, GO, verifier, catalog, ... */
	private String category;

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

    public void merge(SearchResult b) {
		if (StringService.isNullOrEmpty(featureNumber)) {
			featureNumber = b.featureNumber;
		}
		if (!content.contains(b.content)) {
			content += "\n" + b.content;
		}
	}

    public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
