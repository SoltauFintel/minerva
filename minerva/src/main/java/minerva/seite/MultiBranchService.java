package minerva.seite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.base.UserMessage;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;

public class MultiBranchService {

	/**
	 * Copy a page to another workspace
	 * @param seite the source page
	 * @param targetBranch name of the target workspace
	 */
	public void transfer(SeiteSO seite, String targetBranch) {
		UserSO user = seite.getBook().getWorkspace().getUser();
		if (seite.getBook().isFeatureTree()) {
			throw new UserMessage("copyToWorkspaceError1", user);
		}
		WorkspaceSO tw = user.getWorkspace(targetBranch);
		tw.pull();

		// Gibt es das Book im Zielbranch?
		BookSO tb = tw.getBooks()._byFolder(seite.getBook().getBook().getFolder());
		if (tb == null) {
			throw new UserMessage("copyToWorkspaceError2", user);
		}

		// Gibt es die Parent Page im Zielbranch?
		SeiteSO tParentSeite = null;
		if (seite.hasParent()) {
			tParentSeite = tb._seiteById(seite.getSeite().getParentId());
			if (tParentSeite == null) {
				throw new UserMessage("copyToWorkspaceError3", user);
			}
		}
		final var id = seite.getId();
		SeiteSO ts = tb._seiteById(id);
		if (ts == null) { // Seite neu anlegen
			ts = tb.getSeiten().createSeite(tParentSeite == null ? tb.getISeite() : tParentSeite, tb, id);
		} // else: Seite überschreiben   TODO prüfen, ob Inhalte verloren gehen würden
		ts.getSeite().copyFrom_allFields(seite.getSeite());
		for (String dn : seite.getImageFilenames()) {
			var p = "/img/" + id + "/" + dn;
			File src = new File(seite.getBook().getFolder() + p);
			File target = new File(tb.getFolder() + p);
			target.getParentFile().mkdirs();
			try {
				Files.copy(src.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Logger.error("Error copying " + src.getAbsolutePath() + " to " + target.getAbsolutePath());
			}
		}
		ts.saveAll(ts.getSeite().getTitle(), seite.getContent(), seite.getSeite().getVersion(), "",
				MinervaWebapp.factory().getLanguages(), System.currentTimeMillis());
	}
}
