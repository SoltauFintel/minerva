package minerva.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.mail.Mail;
import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.config.MinervaConfig;
import minerva.git.CommitMessage;
import minerva.seite.Note;

public class NotesSO {
    private final SeiteSO seiteSO;
    
    NotesSO(SeiteSO seiteSO) {
        this.seiteSO = seiteSO;
    }
    
    public void addNote(String text, List<String> persons, int parentNumber) {
        int number = addNote(text, persons, parentNumber == 0 ? null : noteByNumber(parentNumber));

        // Send notifications
        MinervaConfig c = MinervaWebapp.factory().getConfig();
        if (!persons.isEmpty() && c.readyForNoteNotifications()) {
            Mail mail = new Mail();
            mail.setSubject(c.getNoteSubject());
            mail.setBody(c.getNoteBody()
                    .replace("{number}", "" + number)
                    .replace("{pageId}", seiteSO.getId())
                    .replace("{pageTitle}", Escaper.esc(seiteSO.getTitle()))
                    .replace("{bookFolder}", Escaper.esc(seiteSO.getBook().getBook().getFolder()))
                    .replace("{branch}", Escaper.esc(seiteSO.getBook().getWorkspace().getBranch())));
            for (String person : persons) {
                mail.setToEmailaddress(c.getMailAddress(person));
                if (!StringService.isNullOrEmpty(mail.getToEmailaddress())) {
                    Logger.info("Note notification for " + person + ". Sending mail to: " + mail.getToEmailaddress());
                    c.sendMail(mail);
                }
else Logger.info("Note notifications. Don't send mail to " + person + " because no mail address is configured."); // XXX DEBUG
            }
        }
else Logger.info("Note notifications. Not configured."); // XXX DEBUG        
    }
    
    private int addNote(String text, List<String> persons, Note parent) {
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
        seiteSO.saveMeta(new CommitMessage(seiteSO, "note #" + note.getNumber() + " added"));
        return note.getNumber();
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
            seiteSO.saveMeta(new CommitMessage(seiteSO, "note #" + number + " deleted"));
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
            note.setDoneBy(done ? seiteSO.getLogin() : null);
            note.setDoneDate(done ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            seiteSO.saveMeta(new CommitMessage(seiteSO, "note #" + number + (done ? " done" : " undone")));
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
