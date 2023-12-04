package minerva.export.pdf;

import java.util.ArrayList;
import java.util.List;

import minerva.model.SeiteSO;

public class Bookmark {
	private final String id;
	private final String title;
	private final List<Bookmark> bookmarks = new ArrayList<>();
	
	public Bookmark(String id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public Bookmark(SeiteSO seite, String lang, Chapter chapter, boolean withChapters) {
		this(seite.getId(), (withChapters ? (chapter.toString() + " ") : "") + seite.getSeite().getTitle().getString(lang));
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public List<Bookmark> getBookmarks() {
		return bookmarks;
	}
}
