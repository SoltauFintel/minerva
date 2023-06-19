package minerva.seite;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private int number;
    /** same as User.login */
    private String user;
    /** format "yyyy-MM-dd HH:mm" */
    private String created;
    /** format "yyyy-MM-dd HH:mm" */
    private String changed;
    private String text;
    private final List<Note> notes = new ArrayList<>();
    private final List<String> persons = new ArrayList<>();
    private boolean done = false;
    private String doneBy;
    
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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
}
