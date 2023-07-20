package minerva.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.mail.Mail;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.config.MinervaConfig;
import minerva.git.CommitMessage;
import minerva.persistence.gitlab.UpToDateCheckService;
import minerva.seite.Note;

public class NotesSO {
    private SeiteSO seite;
    
    NotesSO(SeiteSO seite) {
        this.seite = seite;
    }
    
    public void addNote(String text, List<String> persons, int parentNumber) {
        UpToDateCheckService.check(seite, () -> seite = seite.getMeAsFreshInstance());

        Note note = new Note();
        int number = seite.getSeite().getNextNoteNumber();
        note.setNumber(number);
        seite.getSeite().setNextNoteNumber(number + 1);
        note.setUser(seite.getBook().getUser().getUser().getLogin());
        note.setCreated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        note.setChanged("");
        note.setText(text);
        note.getPersons().addAll(persons);
        if (parentNumber == 0) {
            seite.getSeite().getNotes().add(note);
        } else {
            noteByNumber(parentNumber).getNotes().add(note);
        }
        seite.saveMeta(new CommitMessage(seite, "note #" + note.getNumber() + " added"));
        
        sendNotifications(number, persons);
    }
    
    public Note noteByNumber(int number) {
        Note note = _noteByNumber(seite.getSeite().getNotes(), number);
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
        if (_deleteNote(seite.getSeite().getNotes(), number)) {
            seite.saveMeta(new CommitMessage(seite, "note #" + number + " deleted"));
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
        Note note = _doneNote(seite.getSeite().getNotes(), number);
        if (note == null) {
            Logger.error("Note #" + note + " not found for page ID " + seite.getId());
            throw new RuntimeException("Note not found!");
        } else {
            note.setDone(done);
            note.setDoneBy(done ? seite.getLogin() : null);
            note.setDoneDate(done ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            seite.saveMeta(new CommitMessage(seite, "note #" + number + (done ? " done" : " undone")));
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
        return _getNotesSize(seite.getSeite().getNotes());
    }

    private int _getNotesSize(List<Note> notes) {
        int ret = notes.size();
        for (Note note : notes) {
            ret += _getNotesSize(note.getNotes()); // recursive
        }
        return ret;
    }

    private void sendNotifications(int number, List<String> persons) {
        MinervaConfig c = MinervaWebapp.factory().getConfig();
        if (!persons.isEmpty() && c.readyForNoteNotifications()) {
            Mail mail = new Mail();
            mail.setSubject(c.getNoteSubject());
            mail.setBody(c.getNoteBody()
                    .replace("{number}", "" + number)
                    .replace("{pageId}", seite.getId())
                    .replace("{pageTitle}", seite.getTitle()) // no esc!
                    .replace("{bookFolder}", seite.getBook().getBook().getFolder())
                    .replace("{branch}", seite.getBook().getWorkspace().getBranch()));
            for (String person : persons) {
                mail.setToEmailaddress(c.getMailAddress(person));
                if (!StringService.isNullOrEmpty(mail.getToEmailaddress())) {
                    c.sendMail(mail);
                }
            }
        }
    }

    /**
     * Note has been updated. Notify persons who did not received a notification.
     * @param note contain persons before
     * @param persons after
     */
    public void sendNotifications(Note note, List<String> persons) {
        List<String> newPersons = new ArrayList<>();
        for (String person : persons) {
            if (!note.getPersons().contains(person)) {
                newPersons.add(person);
            }
        }
        sendNotifications(note.getNumber(), newPersons);
    }
}
