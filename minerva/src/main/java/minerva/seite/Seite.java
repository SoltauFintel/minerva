package minerva.seite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import minerva.base.NlsString;
import ohhtml.toc.HelpKeysForHeading;

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
    /**
     * Help keys are trimmed.
     * No empty entries.
     * Commented-out help keys are not planned, but practically possible by prefixing them with characters that break the key.
     */
    private final List<String> helpKeys = new ArrayList<>();
    private List<HelpKeysForHeading> hkh;
    private final List<PageChange> changes = new ArrayList<>();
    private int tocHeadingsLevels = 0;
    private int tocSubpagesLevels = 0;
    private String editorsNote;
    /** cross-book links */
    private final List<String> links = new ArrayList<>();

    public Seite() {
    }
    
    /**
     * copy constructor
     * @param seite -
     */
    // needed for reorder action
    public Seite(Seite c) {
        this.id = c.id;
        this.parentId = c.parentId;
        this.title.from(c.title);
        this.position = c.position;
        this.sorted = c.sorted;
        this.tags.addAll(c.tags);
        this.version = c.version;
        this.helpKeys.addAll(c.helpKeys);
        if (c.hkh == null) {
        	this.hkh = c.hkh;
        } else {
        	this.hkh = new ArrayList<>(c.hkh);
        }
        this.changes.addAll(c.changes);
        this.tocHeadingsLevels = c.tocHeadingsLevels;
        this.tocSubpagesLevels = c.tocSubpagesLevels;
        this.editorsNote = c.editorsNote;
        this.links.addAll(c.links);
    }
    
    // needed for duplicate action
    // not to set: id, parentId, title, position, version, changes, editorsNote.
	public void copyFrom(Seite c, boolean completeCopy) {
	    sorted = c.sorted;
		tocHeadingsLevels = c.tocHeadingsLevels;
		tocSubpagesLevels = c.tocSubpagesLevels;
	    if (completeCopy) {
	    	tags.addAll(c.tags);
	    	helpKeys.addAll(c.helpKeys);
			if (c.hkh == null) {
				hkh = null;
			} else {
				hkh = new ArrayList<>(c.hkh);
			}
			links.addAll(c.links);
	    } else {
	    	tags.clear();
	    	helpKeys.clear();
	    	hkh = null;
	    	links.clear();
	    }
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

    public List<PageChange> getChanges() {
        return changes;
    }

    public List<String> getHelpKeys() {
        return helpKeys;
    }

    /**
     * @return help keys for headings
     */
    public List<HelpKeysForHeading> getHkh() {
        return hkh;
    }

    /**
     * @param hkh help keys for headings
     */
    public void setHkh(List<HelpKeysForHeading> hkh) {
        this.hkh = hkh;
    }

    public int getTocHeadingsLevels() {
        return tocHeadingsLevels;
    }

    public void setTocHeadingsLevels(int tocHeadingsLevels) {
        this.tocHeadingsLevels = tocHeadingsLevels;
    }

    public int getTocSubpagesLevels() {
        return tocSubpagesLevels;
    }

    public void setTocSubpagesLevels(int tocSubpagesLevels) {
        this.tocSubpagesLevels = tocSubpagesLevels;
    }

    public String getEditorsNote() {
        return editorsNote;
    }

    public void setEditorsNote(String editorsNote) {
        this.editorsNote = editorsNote;
    }

    public List<String> getLinks() {
        return links;
    }
}
