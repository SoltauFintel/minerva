package minerva.seite.note;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;
import minerva.base.NLS;
import minerva.seite.Note;
import minerva.seite.SPage;

public class NotesPage extends SPage {
    
    @Override
    protected void execute() {
        Logger.info(user.getUser().getLogin() + " | Notes page for \"" + seite.getTitle() + "\"");
        header(n("notes"));
        put("noteHTML", noteHTML(seite.getSeite().getNotes(), 1));
        put("hasNotes", !seite.getSeite().getNotes().isEmpty());
        put("showTopCreateButton", seite.notes().getNotesSize() >= 4);
    }

    private String noteHTML(List<Note> notes, int ebene) {
        StringBuilder sb = new StringBuilder();
        for (Note note : notes) {
            DataMap m = new DataMap();
            m.put("color", ebene >= 1 && ebene <= 7 ? "E" + ebene : "E");
            m.putInt("number", note.getNumber());
            m.put("user", esc(note.getUser()));
            m.put("created", esc(note.getCreated()));
            m.put("changed", esc(note.getChanged()));
            m.put("hasChanged", !note.getChanged().isEmpty());
            m.put("text", esc(note.getText()));
            fillPersons(note, m);
            m.put("done", note.isDone());
            m.put("doneDate", esc(note.getDoneDate()));
            m.put("notes", noteHTML(note.getNotes(), ebene + 1)); // recursive
            m.put("viewlink", viewlink);
            m.put("addAllowed", ebene < 7);
            m.put("editAllowed", note.getUser().equals(seite.getLogin()));
            m.put("N", "en".equals(user.getGuiLanguage()) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
            sb.append(Page.templates.render("NotePiece", m));
        }
        return sb.toString();
    }

    private void fillPersons(Note note, DataMap m) {
        DataList list = m.list("persons");
        String login = user.getUser().getLogin();
        int max = note.getPersons().size() - 1;
        for (int i = 0; i <= max; i++) {
            String name = note.getPersons().get(i);
            DataMap map = list.add();
            map.put("name", esc(name));
            map.put("last", i == max);
            map.put("me", name.equals(login));
        }
        m.put("hasPersons", !note.getPersons().isEmpty());
        m.put("doneBy", esc(note.getDoneBy()));
    }
}
