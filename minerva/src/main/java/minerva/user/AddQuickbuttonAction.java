package minerva.user;

import org.pmw.tinylog.Logger;

public class AddQuickbuttonAction extends UAction {
	private String link;
	
	@Override
	protected void execute() {
		link = ctx.queryParam("p");
		String label = ctx.queryParam("t");
		String q = ctx.queryParam("q");
		
		label = label.replace(UPage.TITLE_POSTFIX, "");
		if (q != null && !q.isBlank()) {
			if (q.startsWith("__CM")) {
				label = q.substring("__CM".length());
				link += "/" + label;
				if ("null".equals(label)) {
					label = "Kundenmodus aus";
				} else {
					label = label.toUpperCase();
				}
			} else {
				link += "?q=" + u(q);
			}
		}
		user.addQuickbutton(label, link);
		Logger.info(user.getLogin() + " | added quick button: \"" + label + "\", " + link);
		
		ctx.redirect(link);
	}
}
