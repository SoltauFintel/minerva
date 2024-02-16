package minerva.task;

import minerva.comment.Comment;
import minerva.seite.CommentWithSeite;

/**
 * Task adapter for Comment
 */
public class CommentTask implements Task {
    private final CommentWithSeite cws;
    private final Comment comment;
    private final String parentLink;
    private final String link;
    
    public CommentTask(CommentWithSeite cws, String branch) {
        this.cws = cws;
        this.comment = cws.getComment();
        String zt = branch + "/" + cws.getSeite().getBook().getBook().getFolder() + "/" + cws.getSeite().getId();
        parentLink = "/s/" + zt;
        link = "/sc/" + zt + "/comments?highlight=" + comment.getId() + "#" + comment.getId();
    }
    
    @Override
    public String getId() {
        return cws.getSeite().getId() + "-" + comment.getId();
    }

    @Override
    public String getLogin() {
        return comment.getUser();
    }

    @Override
    public String getDateTime() {
        return comment.getCreated();
    }

    @Override
    public String getText() {
        return comment.getText();
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getParentLink() {
        return parentLink;
    }

    @Override
    public String getParentTitle() {
        return cws.getSeite().getTitle();
    }

    @Override
    public String getTypeName() {
        return "Comment"; // it's a RB key
    }

    @Override
    public String getColor() {
        return "rgb(0,101,255)";
    }
}
