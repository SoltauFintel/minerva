package minerva.export.pdf;

import java.util.ArrayList;
import java.util.List;

import minerva.model.SeiteSO;

public class Bookmark {
    private final String id;
    private final String title;
    private final List<Bookmark> bookmarks = new ArrayList<>();
    private final boolean noTree;
    
    public Bookmark(String id, String title, boolean noTree) {
        this.id = id;
        this.title = title;
        this.noTree = noTree;
    }
    
    public Bookmark(SeiteSO seite, String lang, Chapter chapter, boolean withChapters, boolean noTree) {
        this(seite.getId(), (withChapters ? (chapter.toString() + " ") : "") + seite.getSeite().getTitle().getString(lang), noTree);
    }

    public static Bookmark root() {
        return new Bookmark("root", "book", false);
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

    public boolean isNoTree() {
        return noTree;
    }
}
