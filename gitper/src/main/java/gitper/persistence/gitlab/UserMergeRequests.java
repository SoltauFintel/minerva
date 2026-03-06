package gitper.persistence.gitlab;

import java.util.TreeSet;

/**
 * All open merge requests of an user
 */
public class UserMergeRequests {
    /** real name of user */
    private String name;
    /** Set of all MR Id */
    private final TreeSet<Long> idList = new TreeSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeSet<Long> getIdList() {
        return idList;
    }
    
    @Override
    public String toString() {
        return name + idList.toString();
    }
}
