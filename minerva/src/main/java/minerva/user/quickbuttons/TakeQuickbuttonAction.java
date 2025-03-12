package minerva.user;

import org.pmw.tinylog.Logger;

public class TakeQuickbuttonAction extends UAction {
	
	@Override
	protected void execute() {
		String link = ctx.queryParam("p");
		String label = ctx.queryParam("t");

		user.addQuickbutton(label, link);
		Logger.info(user.getLogin() + " | took quick button from other user: \"" + label + "\", " + link);

		ctx.redirect("/q/config");
	}
}
