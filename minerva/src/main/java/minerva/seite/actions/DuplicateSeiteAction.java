package minerva.seite.actions;

import minerva.seite.SAction;

public class DuplicateSeiteAction extends SAction {

	@Override
	protected void execute() {
		ctx.redirect("/s-edit/" + branch + "/" + bookFolder + "/" + seite.duplicate(langs));
	}
}
