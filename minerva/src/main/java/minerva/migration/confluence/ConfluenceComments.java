package minerva.migration.confluence;

import java.util.ArrayList;
import java.util.List;

public class ConfluenceComments {
    private List<ConfluenceComment> comments = new ArrayList<>();

    public List<ConfluenceComment> getComments() {
        return comments;
    }

    public void setComments(List<ConfluenceComment> comments) {
        this.comments = comments;
    }
}
