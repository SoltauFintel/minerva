package minerva.seite;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private String id;
    /** same as User.login */
    private String user;
    /** format "yyyy-MM-dd HH:mm" */
    private String created;
    /** format "yyyy-MM-dd HH:mm", can be null or empty */
    private String changed = "";
    private String text;
    private final transient List<Note> notes = new ArrayList<>();
    private final List<String> persons = new ArrayList<>();
    private boolean done = false;
    /** same as User.login, can be null */
    private String doneBy;
    /** format "yyyy-MM-dd HH:mm", can be null or empty */
    private String doneDate;
    /** null or empty for top level notes, if parent note is missing this note is displayed as a top level note */
    private String parentId = null;
    private transient boolean added = false;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getChanged() {
        return changed;
    }

    public void setChanged(String changed) {
        this.changed = changed;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<String> getPersons() {
        return persons;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDoneBy() {
        return doneBy;
    }

    public void setDoneBy(String doneBy) {
        this.doneBy = doneBy;
    }

    public String getDoneDate() {
        return doneDate;
    }

    public void setDoneDate(String doneDate) {
        this.doneDate = doneDate;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }
}
