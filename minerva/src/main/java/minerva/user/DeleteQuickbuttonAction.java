package minerva.user;

import java.util.List;

public class DeleteQuickbuttonAction extends UAction {

	@Override
	protected void execute() {
		int index = Integer.valueOf(ctx.queryParam("i"));
		
		List<Quickbutton> list = user.getQuickbuttons();
		if (index >= 0 && index < list.size()) {
			list.remove(index);
			user.saveQuickbuttons();
		}
		
		ctx.redirect("/q/config");
	}
}
