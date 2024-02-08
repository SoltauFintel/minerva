package minerva.seite;

import minerva.comment.Comment;
import minerva.model.SeiteSO;

public class CommentWithSeite {
    private final Comment comment;
    private final SeiteSO seite;

    public CommentWithSeite(Comment comment, SeiteSO seite) {
        this.comment = comment;
        this.seite = seite;
    }

    public Comment getNote() {
        return comment;
    }

    public SeiteSO getSeite() {
        return seite;
    }
}
