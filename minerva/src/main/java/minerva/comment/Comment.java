package minerva.comment;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String id;
    /** same as User.login */
    private String user;
    /** format "yyyy-MM-dd HH:mm" */
    private String created;
    /** text changed, format "yyyy-MM-dd HH:mm", can be null or empty */
    private String changed = "";
    private String text = "";
    private String person = "";
    private boolean done = false;
    /** format "yyyy-MM-dd HH:mm", can be null or empty */
    private String doneDate = "";
    /** null or empty for top level comments, if parent comment is missing this comment is displayed as a top level comment */
    private String parentId = "";
    private final transient List<Comment> comments = new ArrayList<>();
    
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

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
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

    public List<Comment> getComments() {
        return comments;
    }
}
