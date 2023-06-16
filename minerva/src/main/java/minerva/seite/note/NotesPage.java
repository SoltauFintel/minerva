package minerva.seite.note;

import java.util.List;

import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;
import minerva.base.NLS;
import minerva.seite.Note;
import minerva.seite.SPage;

public class NotesPage extends SPage {
    
    @Override
    protected void execute() {
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
            m.put("notes", noteHTML(note.getNotes(), ebene + 1)); // recursive
            m.put("viewlink", viewlink);
            m.put("addAllowed", ebene < 7);
            m.put("editAllowed", note.getUser().equals(seite.getLogin()));
            m.put("N", "en".equals(user.getLanguage()) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
            sb.append(Page.templates.render("NotePiece", m));
        }
        return sb.toString();
    }
}
