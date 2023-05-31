package minerva.seite.note;

import minerva.model.SeiteSO;
import minerva.seite.Note;

public class NoteWithSeite {
    private final Note note;
    private final SeiteSO seite;

    public NoteWithSeite(Note note, SeiteSO seite) {
        this.note = note;
        this.seite = seite;
    }

    public Note getNote() {
        return note;
    }

    public SeiteSO getSeite() {
        return seite;
    }
}
