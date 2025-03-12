package minerva.user;

import minerva.workspace.WAction;

public class ToggleQuickbuttonsAction extends WAction {

	@Override
	protected void execute() {
		user.toggleQuickbuttons();
		ctx.redirect("/w/" + workspace.getBranch() + "/menu");
	}
}
