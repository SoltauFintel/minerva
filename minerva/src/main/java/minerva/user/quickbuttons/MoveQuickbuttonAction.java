package minerva.user.quickbuttons;

import java.util.List;

import minerva.user.UAction;

public class MoveQuickbuttonAction extends UAction {

	@Override
	protected void execute() {
		int index = Integer.valueOf(ctx.queryParam("i"));
		int d = Integer.valueOf(ctx.queryParam("d"));

		List<Quickbutton> list = user.getQuickbuttons();
		int min = d == -1 ? 1 : 0;
		int max = d == -1 ? list.size() - 1 : list.size() - 2;
		if (index >= min && index <= max && (d == -1 || d == 1)) { // move up or down
			Quickbutton qb = list.get(index);
			list.remove(index);
			list.add(index + d, qb);
			user.saveQuickbuttons();
		} else if (index >= 0 && index < list.size() && d == 0) { // toggle onlyMe
			Quickbutton qb = list.get(index);
			qb.setOnlyMe(!qb.isOnlyMe());
			user.saveQuickbuttons();
		}
		
		ctx.redirect("/q/config");
	}
}
