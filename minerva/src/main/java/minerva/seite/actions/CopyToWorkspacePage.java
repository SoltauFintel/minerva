package minerva.seite.actions;

import minerva.base.UserMessage;
import minerva.seite.MultiBranchService;
import minerva.seite.SPage;

public class CopyToWorkspacePage extends SPage {

	@Override
	protected void execute() {
		if (isPOST()) {
			String targetBranch = ctx.formParam("targetBranch");
			user.setTargetBranch(targetBranch);
			new MultiBranchService().transfer(seite, targetBranch);
			user.log("copy to workspace " + targetBranch + ": " + viewlink);
			ctx.redirect(viewlink);
		} else {
			var targetBranchs = user.getWorkspaces().getBranches();
			targetBranchs.remove(branch);
			if (targetBranchs.isEmpty()) {
				throw new UserMessage("noOtherWorkspace", user);
			}
			combobox("targetBranchs", targetBranchs, user.getTargetBranch(), false);
			header(n("copyToWorkspace"));
		}
	}
}
