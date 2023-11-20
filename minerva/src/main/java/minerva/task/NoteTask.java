package minerva.task;

import minerva.seite.Note;
import minerva.seite.note.NoteWithSeite;

/**
 * Task adapter for Note
 */
public class NoteTask implements Task {
	private final NoteWithSeite n;
	private final Note note;
	private final String link;
	
	public NoteTask(NoteWithSeite n, String branch) {
		this.n = n;
		this.note = n.getNote();
		link = "/s/" + branch + "/" + n.getSeite().getBook().getBook().getFolder() + "/" + n.getSeite().getId();
	}
	
	@Override
	public String getId() {
		return n.getSeite().getId() + "-" + note.getId();
	}

	@Override
	public String getLogin() {
		return note.getUser();
	}

	@Override
	public String getDateTime() {
		return note.getCreated();
	}

	@Override
	public String getText() {
		return note.getText();
	}

	@Override
	public String getLink() {
		return link + "/notes?highlight=" + note.getId() + "#" + note.getId();
	}

	@Override
	public String getParentLink() {
		return link;
	}

	@Override
	public String getParentTitle() {
		return n.getSeite().getTitle();
	}

	@Override
	public String getTypeName() {
		return "Kommentar";
	}

	@Override
	public String getColor() {
		return "rgb(0,101,255)";
	}
}