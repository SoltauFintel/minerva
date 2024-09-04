package minerva.mask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.book.BAction;
import minerva.model.SeiteSO;

public class FeatureFieldsFilesCleanupAction extends BAction {

	@Override
	protected void execute() {
		user.onlyAdmin();
		if (!book.isFeatureTree()) {
			throw new RuntimeException("Only for feature tree");
		}

		final String bf = book.getFolder() + "/";
		Set<String> filenames = book.dao().getFilenames(bf + "feature-fields");
		Set<String> existing = new HashSet<>();
		Set<String> kill = new HashSet<>();
		for (SeiteSO ft : book.getAlleSeiten()) {
			existing.add(ft.getId() + ".ff");
		}
		Logger.info(bf + " | .ff Dateileichen löschen");
		int ok = 0;
		for (String dn : filenames) {
			if (existing.contains(dn)) {
				ok++;
			} else {
				kill.add(bf + "feature-fields/" + dn);
			}
		}
		Logger.info("Anzahl: " + kill.size() + " | ok: " + ok);
		if (!kill.isEmpty()) {
			List<String> cant = new ArrayList<>();
			book.dao().deleteFiles(kill, new CommitMessage("Delete unused *.ff files"), workspace, cant);
			if (cant.isEmpty()) {
				Logger.info("Löschen erfolgreich");
			} else {
				Logger.error("Konnte nicht gelöscht werden: " + cant.toString());
			}
		}

		// TODO Wenn Features neu importiert werden, müssen auch die alten .ff Dateien gelöscht werden.

		ctx.redirect("/");
	}
}
