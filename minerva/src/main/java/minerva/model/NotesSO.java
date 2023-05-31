package minerva.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import minerva.seite.Note;

public class NotesSO {
    private final SeiteSO seiteSO;
    
    NotesSO(SeiteSO seiteSO) {
        this.seiteSO = seiteSO;
    }
    
    public void addNote(String text, Note parent) {
        Note note = new Note();
        // TODO zuletzt vergebene number in der Seite merken
        note.setNumber(1 + fetchMax(seiteSO.getSeite().getNotes()));
        note.setUser(seiteSO.getBook().getUser().getUser().getLogin());
        note.setCreated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        note.setChanged("");
        note.setText(text);
        if (parent == null) {
            seiteSO.getSeite().getNotes().add(note);
        } else {
            parent.getNotes().add(note);
        }
        seiteSO.saveMeta(seiteSO.getTitle() + ": add note #" + note.getNumber());
    }

    private int fetchMax(List<Note> notes) {
        int max = 0;
        for (Note note : notes) {
            if (note.getNumber() > max) {
                max = note.getNumber();
            }
            int m = fetchMax(note.getNotes());
            if (m > max) {
                max = m;
            }
        }
        return max;
    }

    public Note noteByNumber(int number) {
        Note note = _noteByNumber(seiteSO.getSeite().getNotes(), number);
        if (note == null) {
            throw new RuntimeException("Note not found");
        }
        return note;
    }

    private Note _noteByNumber(List<Note> notes, int number) {
        for (Note note : notes) {
            if (note.getNumber() == number) {
                return note;
            }
            Note note2 = _noteByNumber(note.getNotes(), number);
            if (note2 != null) {
                return note2;
            }
        }
        return null;
    }
    
    public void deleteNote(int number) {
        if (_deleteNote(seiteSO.getSeite().getNotes(), number)) {
            seiteSO.saveMeta(seiteSO.getTitle() + ": delete note #" + number);
        }
    }
    
    private boolean _deleteNote(List<Note> notes, int number) {
        for (Note note : notes) {
            if (note.getNumber() == number) {
                notes.remove(note);
                return true;
            }
            boolean ret = _deleteNote(note.getNotes(), number);
            if (ret) {
                return ret;
            }
        }
        return false;
    }
    
    public int getNotesSize() {
        return _getNotesSize(seiteSO.getSeite().getNotes());
    }

    private int _getNotesSize(List<Note> notes) {
        int ret = notes.size();
        for (Note note : notes) {
            ret += _getNotesSize(note.getNotes());
        }
        return ret;
    }
}
