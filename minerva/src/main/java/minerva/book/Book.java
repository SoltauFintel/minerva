package minerva.book;

import minerva.base.NlsString;

/**
 * Ein Book enthält Seiten.
 * Die hierarchischen Seiten ergeben eine Gliederung.
 */
public class Book {
    private NlsString title;
    private String folder;
    private int position;
    private boolean sorted = false;
    private BookType type = BookType.PUBLIC;
    
    public NlsString getTitle() {
        return title;
    }

    public void setTitle(NlsString title) {
        this.title = title;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
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

    public BookType getType() {
        return type;
    }

    public void setType(BookType type) {
        this.type = type;
    }
}
