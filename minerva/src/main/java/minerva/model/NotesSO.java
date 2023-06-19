package minerva.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.seite.Note;

public class NotesSO {
    private final SeiteSO seiteSO;
    
    NotesSO(SeiteSO seiteSO) {
        this.seiteSO = seiteSO;
    }
    
    public void addNote(String text, List<String> persons, Note parent) {
        Note note = new Note();
        int next = seiteSO.getSeite().getNextNoteNumber();
        note.setNumber(next);
        seiteSO.getSeite().setNextNoteNumber(next + 1);
        note.setUser(seiteSO.getBook().getUser().getUser().getLogin());
        note.setCreated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        note.setChanged("");
        note.setText(text);
        note.getPersons().addAll(persons);
        if (parent == null) {
            seiteSO.getSeite().getNotes().add(note);
        } else {
            parent.getNotes().add(note);
        }
        seiteSO.saveMeta(seiteSO.getTitle() + ": add note #" + note.getNumber());
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
            Note note2 = _noteByNumber(note.getNotes(), number); // recursive
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
            boolean ret = _deleteNote(note.getNotes(), number); // recursive
            if (ret) {
                return ret;
            }
        }
        return false;
    }
    
    public void doneNote(int number, boolean done) {
        Note note = _doneNote(seiteSO.getSeite().getNotes(), number);
        if (note == null) {
            Logger.error("Note #" + note + " not found for page ID " + seiteSO.getId());
            throw new RuntimeException("Note not found!");
        } else {
            note.setDone(done);
            note.setDoneBy(seiteSO.getLogin());
            note.setChanged(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            seiteSO.saveMeta(seiteSO.getTitle() + ": " + (done ? "" : "un") + "done note #" + number);
        }
    }

    private Note _doneNote(List<Note> notes, int number) {
        for (Note note : notes) {
            if (note.getNumber() == number) {
                return note;
            }
            Note n = _doneNote(note.getNotes(), number); // recursive
            if (n != null) {
                return n;
            }
        }
        return null;
    }

    public int getNotesSize() {
        return _getNotesSize(seiteSO.getSeite().getNotes());
    }

    private int _getNotesSize(List<Note> notes) {
        int ret = notes.size();
        for (Note note : notes) {
            ret += _getNotesSize(note.getNotes()); // recursive
        }
        return ret;
    }
}
