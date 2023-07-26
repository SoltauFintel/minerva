package minerva.seite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import minerva.base.NlsString;

/**
 * Entity for holding page meta data.
 * It is called Seite (German) so that there is no confusion with the amalia Page concept.
 */
public class Seite {
    private String id;
    private String parentId;
    private final NlsString title = new NlsString();
    private int position;
    private boolean sorted = true;
    private final Set<String> tags = new TreeSet<>();
    private int version = 1;
    // TODO Leerzeilen? Kommentare erlaubt? trim?
    private final List<String> helpKeys = new ArrayList<>();
    private final transient List<Note> notes = new ArrayList<>();
    private final List<PageChange> changes = new ArrayList<>();
    private int tocLevels = 0;
    private boolean tocWithSubpages = false;

    public Seite() {
    }
    
    /**
     * copy constructor
     * @param seite -
     */
    public Seite(Seite c) {
        this.id = c.id;
        this.parentId = c.parentId;
        this.title.from(c.title);
        this.position = c.position;
        this.sorted = c.sorted;
        this.tags.addAll(c.tags);
        this.version = c.version;
        this.helpKeys.addAll(c.helpKeys);
        this.notes.addAll(c.notes);
        this.changes.addAll(c.changes);
    }

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

    public NlsString getTitle() {
        return title;
    }

    public Set<String> getTags() {
        return tags;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<PageChange> getChanges() {
        return changes;
    }

    public List<String> getHelpKeys() {
        return helpKeys;
    }

    public int getTocLevels() {
        return tocLevels;
    }

    public void setTocLevels(int tocLevels) {
        this.tocLevels = tocLevels;
    }

    public boolean isTocWithSubpages() {
        return tocWithSubpages;
    }

    public void setTocWithSubpages(boolean tocWithSubpages) {
        this.tocWithSubpages = tocWithSubpages;
    }
}
