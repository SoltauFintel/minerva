package minerva.user.quickbuttons;

import org.pmw.tinylog.Logger;

import minerva.user.UAction;
import minerva.user.UPage;

public class AddQuickbuttonAction extends UAction {
	private String link;
	
	@Override
	protected void execute() {
		link = ctx.queryParam("p");
		String label = ctx.queryParam("t");
		String q = ctx.queryParam("q");
		
		label = label.replace(UPage.TITLE_POSTFIX, "").replace("GTC-Auswertungen", "GTC-Ausw.");
		if (label.endsWith(" - GTC")) {
			label = label.substring(0, label.length() - " - GTC".length());
		}
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
		if (!"/q/config".equals(link)) {
			user.addQuickbutton(label, link);
			Logger.info(user.getLogin() + " | added quick button: \"" + label + "\", " + link);
		}
		
		ctx.redirect(link);
	}
}
