package minerva.workspace;

import minerva.MinervaWebapp;
import minerva.user.UAction;

public class PullWorkspace extends UAction {

	@Override
	protected void execute() {
		if (!MinervaWebapp.factory().isGitlab()) {
			throw new RuntimeException("Page only for Gitlab mode");
		}

		String branch = ctx.pathParam("branch");
		boolean force = "1".equals(ctx.queryParam("force"));
		
		user.getWorkspace(branch).pull(force);
		
		ctx.redirect("/b/" + branch);
	}
}
