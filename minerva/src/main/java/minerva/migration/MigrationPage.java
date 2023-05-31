package minerva.migration;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class MigrationPage extends UPage {

	@Override
	protected void execute() {
		String branch = ctx.pathParam("branch");
		String sourceFolder = "/";
//		String sourceFolder = "/dat/online-help";

		if ("master".equals(branch)) {
			throw new RuntimeException("Migration nicht für master erlaubt! (Schutz)");
		}
		if ("1".equals(ctx.queryParam("m"))) {
			WorkspaceSO workspace = user.getWorkspace(branch);
			try {
				new ConfluenceToMinervaMigrationService(new File(sourceFolder), workspace, langs).migrate();
			} catch (Exception e) {
				Logger.error(e);
				throw new RuntimeException("Migration error. See log.");
			}
			ctx.redirect("/b/"+ branch);
		}
		
		put("branch", esc(branch));
		put("sourceFolder", new File(sourceFolder).getAbsolutePath());
	}
}
