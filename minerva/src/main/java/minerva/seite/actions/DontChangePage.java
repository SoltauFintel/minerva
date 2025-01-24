package minerva.seite.actions;

import minerva.seite.SPage;

/**
 * Write protection page for pages of book Release Notes
 */
public class DontChangePage extends SPage {

	@Override
	protected void execute() {
		header(n("Schreibschutz") + ": " + seite.getTitle());
		put("message", esc(n("SchreibschutzMsg").replace("$b", book.getTitle())));
		String partlink = branch + "/" + bookFolder + "/" + seite.getId();
		put("link1", esc("/s-edit/" + partlink));
		put("button1", n("locked.edit1.self"));
		put("link2", esc("/s/" + partlink));
		put("button2", n("locked.no-edit"));
	}
}
