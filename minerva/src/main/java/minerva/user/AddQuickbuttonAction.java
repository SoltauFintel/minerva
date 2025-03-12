package minerva.user;

import java.util.List;

public class AddQuickbuttonAction extends UAction {

	@Override
	protected void execute() {
		String path = ctx.queryParam("p");
		String title = ctx.queryParam("t");
		
		List<Quickbutton> buttons = user.getQuickbuttons();
		buttons.removeIf(i -> i.getLink().equals(path));
		Quickbutton b = new Quickbutton();
		b.setLink(path);
		b.setLabel(title.replace(" - Minerva", ""));
		buttons.add(b);
		user.saveQuickbuttons();
		
		ctx.redirect(path);
	}
}
