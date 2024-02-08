package minerva.seite;

import minerva.comment.Comment;
import minerva.model.SeiteSO;

public class NoteWithSeite {
    private final Comment note;
    private final SeiteSO seite;

    public NoteWithSeite(Comment note, SeiteSO seite) {
        this.note = note;
        this.seite = seite;
    }

    public Comment getNote() {
        return note;
    }

    public SeiteSO getSeite() {
        return seite;
    }
}
