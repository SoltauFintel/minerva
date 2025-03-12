package minerva.user;

import java.util.List;

public class EditQuickbuttonPage extends UPage {

	@Override
	protected void execute() {
		int index = Integer.valueOf(ctx.queryParam("i"));

		List<Quickbutton> list = user.getQuickbuttons();
		if (index < 0 || index >= list.size()) {
			throw new RuntimeException("Wrong index");
		}
		Quickbutton qb = list.get(index);
		if (isPOST()) {
			String label = ctx.formParam("label");
			String link = ctx.formParam("link");
			
			qb.setLabel(label);
			qb.setLink(link);
			user.saveQuickbuttons();
			
			ctx.redirect("/q/config");
		} else {
			header(n("editQuickbutton"));
			put("label", esc(qb.getLabel()));
			put("link", esc(qb.getLink()));
		}
	}
}
