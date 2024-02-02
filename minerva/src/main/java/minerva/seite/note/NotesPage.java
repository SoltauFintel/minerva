package minerva.seite.note;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;
import minerva.base.NLS;
import minerva.base.StringService;
import minerva.base.Uptodatecheck;
import minerva.seite.Note;
import minerva.seite.SPage;
import minerva.user.UserAccess;

public class NotesPage extends SPage implements Uptodatecheck {
    
    @Override
    protected void execute() {
		String highlight = ctx.queryParam("highlight");
        Logger.info(user.getLogin() + " | " + branch + " | Notes page for \"" + seite.getTitle() + "\"");
        header(n("notes") + " - " + seite.getTitle());
        put("noteHTML", noteHTML(seite.getSeite().getNotes(), highlight, 1));
        put("hasNotes", !seite.getSeite().getNotes().isEmpty());
        put("showTopCreateButton", seite.notes().getNotesSize() >= 4);
        int openNotes = seite.notes().getOpenNotesSize();
        put("openNotes", openNotes == 0 ? "" : openNotes + " " + (openNotes == 1 ? n("openNote") : n("openNotes")));
    }

    private String noteHTML(List<Note> notes, String highlight, int ebene) {
        StringBuilder sb = new StringBuilder();
        for (Note note : notes) {
            DataMap m = new DataMap();
            m.put("color", ebene >= 1 && ebene <= 7 ? "E" + ebene : "E");
            m.put("noteId", note.getId());
            m.put("user", esc(UserAccess.login2RealName(note.getUser())));
            m.put("created", esc(note.getCreated()));
            m.put("changed", esc(note.getChanged()));
            m.put("hasChanged", !note.getChanged().isEmpty());
            m.put("text", StringService.makeClickableLinks(esc(note.getText())));
            fillPersons(note, m);
            m.put("done", note.isDone());
            m.put("allDone", ebene == 1 && allDone(note));
            m.put("ebene1", ebene == 1);
            boolean hasNoSubnotes = note.getNotes().isEmpty();
            m.put("showThreadLabel", hasNoSubnotes ? n("showNote") : n("showThread"));
            m.put("hideThreadLabel", hasNoSubnotes ? n("hideNote") : n("hideThread"));
            m.put("doneDate", esc(note.getDoneDate()));
            m.put("doneBy", esc(UserAccess.login2RealName(note.getDoneBy())));
            m.put("notes", noteHTML(note.getNotes(), highlight, ebene + 1)); // recursive
            m.put("viewlink", viewlink);
            m.put("addAllowed", ebene < 7);
            m.put("editAllowed", note.getUser().equals(seite.getLogin()) || isAdmin);
            m.put("N", "en".equals(user.getGuiLanguage()) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
            m.put("highlight", note.getId().equals(highlight));
            m.put("me", note.getUser().equals(user.getLogin()));
            sb.append(Page.templates.render("NotePiece", m));
        }
        return sb.toString();
    }

	private boolean allDone(Note note) {
        return note.isDone() && !note.getNotes().stream().anyMatch(n -> !n.isDone());
    }

    private void fillPersons(Note note, DataMap m) {
        DataList list = m.list("persons");
        String login = user.getLogin();
        int max = note.getPersons().size() - 1;
        for (int i = 0; i <= max; i++) {
            String name = note.getPersons().get(i);
            DataMap map = list.add();
            map.put("name", esc(UserAccess.login2RealName(name)));
            map.put("last", i == max);
            map.put("me", name.equals(login));
        }
        m.put("hasPersons", !note.getPersons().isEmpty());
    }
}
