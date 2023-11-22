package minerva.seite.note;

import org.pmw.tinylog.Logger;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.Note;
import minerva.workspace.WAction;

public class NotesBugfix extends WAction {

	@Override
	protected void execute() {
		Logger.info("notes bugfix...");
		for (BookSO bookSO : workspace.getBooks()) {
			for (SeiteSO seiteSO : bookSO.getAlleSeiten()) {
				for (Note note : seiteSO.getSeite().getNotes()) {
					if (note.getPersons() != null && !note.getPersons().isEmpty()) {
						System.out.println(note.getPersons());
					}
				}
			}
		}
		Logger.info("<< notes bugfix <<");
	}
}
