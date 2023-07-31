package minerva.seite.note;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;
import minerva.MinervaWebapp;
import minerva.base.NLS;
import minerva.base.Uptodatecheck;
import minerva.seite.Note;
import minerva.seite.SPage;

public class NotesPage extends SPage implements Uptodatecheck {
    
    @Override
    protected void execute() {
        Logger.info(user.getLogin() + " | Notes page for \"" + seite.getTitle() + "\"");
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
            m.put("noteId", note.getId());
            m.put("user", esc(MinervaWebapp.factory().login2RealName(note.getUser())));
            m.put("created", esc(note.getCreated()));
            m.put("changed", esc(note.getChanged()));
            m.put("hasChanged", !note.getChanged().isEmpty());
            m.put("text", makeLinks(esc(note.getText())));
            fillPersons(note, m);
            m.put("done", note.isDone());
            m.put("doneDate", esc(note.getDoneDate()));
            m.put("doneBy", esc(MinervaWebapp.factory().login2RealName(note.getDoneBy())));
            m.put("notes", noteHTML(note.getNotes(), ebene + 1)); // recursive
            m.put("viewlink", viewlink);
            m.put("addAllowed", ebene < 7);
            m.put("editAllowed", note.getUser().equals(seite.getLogin()) || isAdmin);
            m.put("N", "en".equals(user.getGuiLanguage()) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
            sb.append(Page.templates.render("NotePiece", m));
        }
        return sb.toString();
    }

    private String makeLinks(String text) {
		Pattern regex = Pattern.compile("\\((.*)\\)\\[(.*)\\]", Pattern.MULTILINE);
		Matcher matcher = regex.matcher(text);
		while (matcher.find()) {
			String url = matcher.group(2);
			String target = "";
			if (url.startsWith("http://") || url.startsWith("https://")) {
				target = " target=\"_blank\"";
			} else {
				url = "../" + url;
			}
			text = text.replace(matcher.group(0), "<a href=\"" + url + "\"" + target + ">" + matcher.group(1) + "</a>");
		}
		return text;
	}

	private void fillPersons(Note note, DataMap m) {
        DataList list = m.list("persons");
        String login = user.getLogin();
        int max = note.getPersons().size() - 1;
        for (int i = 0; i <= max; i++) {
            String name = note.getPersons().get(i);
            DataMap map = list.add();
            map.put("name", esc(MinervaWebapp.factory().login2RealName(name)));
            map.put("last", i == max);
            map.put("me", name.equals(login));
        }
        m.put("hasPersons", !note.getPersons().isEmpty());
    }
}
