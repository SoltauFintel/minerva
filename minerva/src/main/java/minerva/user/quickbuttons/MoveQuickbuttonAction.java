package minerva.user;

import java.util.List;

public class MoveQuickbuttonAction extends UAction {

	@Override
	protected void execute() {
		int index = Integer.valueOf(ctx.queryParam("i"));
		int d = Integer.valueOf(ctx.queryParam("d"));

		List<Quickbutton> list = user.getQuickbuttons();
		int min = d == -1 ? 1 : 0;
		int max = d == -1 ? list.size() - 1 : list.size() - 2;
		if (index >= min && index <= max && (d == -1 || d == 1)) {
			Quickbutton item = list.get(index);
			list.remove(index);
			list.add(index + d, item);
			user.saveQuickbuttons();
		}
		
		ctx.redirect("/q/config");
	}
}
