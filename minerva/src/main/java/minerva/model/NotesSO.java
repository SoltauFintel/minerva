package minerva.model;

import static minerva.base.StringService.now;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.mail.Mail;
import minerva.MinervaWebapp;
import minerva.access.CommitMessage;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.StringService;
import minerva.config.MinervaConfig;
import minerva.seite.Note;

public class NotesSO {
    private static final String NOTES_FOLDER = "notes";
    private SeiteSO seite;
    
    NotesSO(SeiteSO seite) {
        this.seite = seite;
    }
   
    public void addNote(String text, List<String> persons, String parentId) {
        Note parentNote = parentId == null ? null : find(parentId);
        Note note = createNote(parentNote, text, seite.getBook().getUser().getLogin(), now(), persons);
        saveNote(note, new CommitMessage(seite, "note added"));
        
        sendNotifications(note.getId(), persons);
    }
    
    public Note createNote(Note parentNote, String text, String user, String created, List<String> persons) {
        Note note = new Note();
        note.setId(IdGenerator.createId6());
        note.setParentId(parentNote == null ? "" : parentNote.getId());
        note.setUser(user);
        note.setCreated(created);
        note.setText(text);
        note.getPersons().addAll(persons);
        if (parentNote == null) {
            seite.getSeite().getNotes().add(note);
        } else {
            parentNote.getNotes().add(note);
        }
        return note;
    }
    
    public void saveTo(Note note, Map<String, String> files) {
    	files.put(filename(note), StringService.prettyJSON(note));
    }
    
    public void deleteNote(String id) {
        Note note = find(id);
        if (note != null) {
            Set<String> filenames = new HashSet<>();
            collectFilenames(note, filenames);
            findParentCollection(id).remove(note);
            _deleteNotes(filenames);
        }
    }
    
    private void collectFilenames(Note note, Set<String> filenames) {
        filenames.add(filename(note));
        for (Note sub : note.getNotes()) {
            collectFilenames(sub, filenames); // recursive
        }
    }

    private void _deleteNotes(Set<String> filenames) {
        BookSO book = seite.getBook();
        List<String> cantBeDeleted = new ArrayList<>();
        book.dao().deleteFiles(filenames, new CommitMessage("note deleted"), book.getWorkspace(), cantBeDeleted);
        if (!cantBeDeleted.isEmpty()) {
            throw new RuntimeException("These note files can't be deleted: " + filenames);
        }
    }
    
    public void doneNote(String id, boolean done) {
        Note note = find(id);
        if (note == null) {
            Logger.error("Note #" + id + " not found for page ID " + seite.getId());
            throw new RuntimeException("Note not found!");
        } else {
            note.setDone(done);
            note.setDoneBy(done ? seite.getLogin() : null);
            note.setDoneDate(done ? now() : null);
            saveNote(note, new CommitMessage(seite, "note " + (done ? "done" : "undone")));
        }
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

    public int getOpenNotesSize() {
        return _getOpenNotesSize(seite.getSeite().getNotes());
    }

    private int _getOpenNotesSize(List<Note> notes) {
        int ret = 0;
        for (Note note : notes) {
            if (!note.isDone()) {
                ret++;
            }
            ret += _getOpenNotesSize(note.getNotes()); // recursive
        }
        return ret;
    }

    private void sendNotifications(String noteId, List<String> persons) {
        MinervaConfig c = MinervaWebapp.factory().getConfig();
        if (!persons.isEmpty() && c.readyForNoteNotifications()) {
            Mail mail = new Mail();
            mail.setSubject(c.getNoteSubject());
            mail.setBody(c.getNoteBody()
                    .replace("{noteId}", noteId)
                    .replace("{pageId}", seite.getId())
                    .replace("{pageTitle}", seite.getTitle()) // no esc!
                    .replace("{bookFolder}", seite.getBook().getBook().getFolder())
                    .replace("{branch}", seite.getBook().getWorkspace().getBranch()));
            String login = seite.getLogin();
            for (String person : persons) {
                mail.setToEmailaddress(c.getMailAddress(person));
                if (!StringService.isNullOrEmpty(mail.getToEmailaddress()) && !person.equals(login)) {
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
        sendNotifications(note.getId(), newPersons);
    }
    
    public Note find(String id) {
        return find(seite.getSeite().getNotes(), id);
    }

    private Note find(List<Note> notes, String id) {
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                return note;
            }
            Note x = find(note.getNotes(), id); // recursive
            if (x != null) {
                return x;
            }
        }
        return null;
    }

    private List<Note> findParentCollection(String id) {
        return findParentCollection(seite.getSeite().getNotes(), id);
    }

    private List<Note> findParentCollection(List<Note> notes, String id) {
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                return notes;
            }
            List<Note> x = findParentCollection(note.getNotes(), id); // recursive
            if (x != null) {
                return x;
            }
        }
        return null;
    }
    
    private String filename(Note note) {
        return folder() + "/" + note.getId() + ".json";
    }
    
    private String folder() {
        return seite.getBook().getFolder() + "/" + NOTES_FOLDER + "/" + seite.getId();
    }

    public void load() {
        Map<String, String> files = seite.getBook().dao().loadAllFiles(folder());
        
        List<Note> allNotes = new ArrayList<>();
        for (Entry<String, String> e : files.entrySet()) {
            allNotes.add(new Gson().fromJson(e.getValue(), Note.class));
        }
        
        seite.getSeite().getNotes().clear();
        addLoadedNote("", seite.getSeite().getNotes(), allNotes);
        for (Note note : allNotes) { // add not assigned notes as top level notes
            if (!note.isAdded()) {
                seite.getSeite().getNotes().add(note);
            }
        }
    }
    
    private void addLoadedNote(String parentId, List<Note> notes, List<Note> allNotes) {
        Iterator<Note> iter = allNotes.iterator();
        while (iter.hasNext()) {
            Note note = iter.next();
            if (note.getParentId().equals(parentId)) {
                notes.add(note);
                note.setAdded(true);
                addLoadedNote(note.getId(), note.getNotes(), allNotes); // recursive
                notes.sort((a, b) -> a.getCreated().compareTo(b.getCreated()));
            }
        }
    }

    public void saveEditedNote(String text, List<String> persons, Note note) {
        if (!note.getText().equals(text) || !note.getPersons().equals(persons)) {
            note.setText(text);
            seite.notes().sendNotifications(note, persons);
            note.getPersons().clear();
            note.getPersons().addAll(persons);
            note.setChanged(now());
            saveNote(note, new CommitMessage(seite, "note modified"));
        }
    }

    private void saveNote(Note note, CommitMessage cm) {
        BookSO book = seite.getBook();
        MultiPurposeDirAccess dao = new MultiPurposeDirAccess(book.dao());
        dao.save(filename(note), note, cm, book.getWorkspace());
    }
}
