package minerva.mask;

import java.util.HashSet;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.book.BAction;
import minerva.model.SeiteSO;

public class FeatureFieldsFilesCleanupAction extends BAction {

	@Override
	protected void execute() {
		user.onlyAdmin();
		if (!book.isFeatureTree()) {
			throw new RuntimeException("Only for feature tree");
		}
		
		Set<String> filenames = book.dao().getFilenames(book.getFolder() + "/feature-fields");
		Set<String> existing = new HashSet<>();
		for (SeiteSO ft : book.getAlleSeiten()) {
			existing.add(ft.getId() + ".ff");
		}
		Logger.info(book.getFolder() + " | Diese .ff Dateien sind Leichen:");
		int n = 0, ex = 0;
		for (String dn : filenames) {
			if (!existing.contains(dn)) {
				Logger.info(dn);
				n++;
			} else {
				ex++;
			}
		}
		Logger.info("Anzahl: " + n + " | ok: " + ex);
		
		// TODO Wenn Features neu importiert werden bzw. gelöscht werden, muss auch die .ff Datei gelöscht werden.

		ctx.redirect("/");
	}
}
