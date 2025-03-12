package minerva.user;

import java.util.List;

import org.pmw.tinylog.Logger;

public class AddQuickbuttonAction extends UAction {
	private String path;
	
	@Override
	protected void execute() {
		path = ctx.queryParam("p");
		String title = ctx.queryParam("t");
		String q = ctx.queryParam("q");
		
		title = title.replace(UPage.TITLE_POSTFIX, "");
		List<Quickbutton> buttons = user.getQuickbuttons();
		if (q != null && !q.isBlank()) {
			if (q.startsWith("__CM")) {
				title = q.substring("__CM".length());
				path += "/" + title;
				if ("null".equals(title)) {
					title = "Kundenmodus aus";
				} else {
					title = title.toUpperCase();
				}
			} else {
				path += "?q=" + u(q);
			}
		}
		buttons.removeIf(i -> i.getLink().equals(path));
		Quickbutton b = new Quickbutton();
		b.setLink(path);
		b.setLabel(title);
		buttons.add(b);
		user.saveQuickbuttons();
		Logger.info(user.getLogin() + " | added quick button: \"" + title + "\", " + path);
		
		ctx.redirect(path);
	}
}
