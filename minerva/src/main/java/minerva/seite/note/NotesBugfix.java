package minerva.seite.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.Note;
import minerva.user.UserAccess;
import minerva.workspace.WAction;

public class NotesBugfix extends WAction {

	@Override
	protected void execute() {
		Logger.info("notes bugfix...");
		Map<String, String> files = new HashMap<>();
		for (BookSO bookSO : workspace.getBooks()) {
			for (SeiteSO seiteSO : bookSO.getAlleSeiten()) {
				for (Note note : seiteSO.getSeite().getNotes()) {
					if (note.getPersons() != null && !note.getPersons().isEmpty()) {
						List<String> add = new ArrayList<>();
						List<String> remove = new ArrayList<>();
						for (String person : note.getPersons()) {
							if (person.contains(" ")) { // it's a real name
								String login = UserAccess.realName2Login(person);
								if (!person.equals(login)) {
									remove.add(person);
									add.add(login);
								}
							}
						}
						if (!remove.isEmpty()) {
							note.getPersons().removeAll(remove);
							note.getPersons().addAll(add);
							seiteSO.notes().saveTo(note, files);
						}
					}
				}
			}
		}
		if (!files.isEmpty()) {
			workspace.dao().saveFiles(files, new CommitMessage("note.persons bugfix"), workspace);
		}
		Logger.info("<< notes bugfix << " + files.size());
	}
}
