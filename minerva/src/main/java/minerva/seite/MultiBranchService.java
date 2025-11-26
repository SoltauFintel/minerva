package minerva.seite;

import minerva.MinervaWebapp;
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
		if (seite.getBook().isFeatureTree()) {
			throw new RuntimeException("Not allowed for feature tree.");
		}
		final var id = seite.getId();
		UserSO user = seite.getBook().getWorkspace().getUser();
		WorkspaceSO tw = user.getWorkspace(targetBranch);
		tw.pull();

		// Gibt es das Book im Zielbranch?
		BookSO tb = tw.getBooks()._byFolder(seite.getBook().getBook().getFolder());
		if (tb == null) {
			throw new RuntimeException("Can not transfer page to target branch. Missing book.");
		}

		// Gibt es die Parent Page im Zielbranch?
		SeiteSO tParentSeite = null;
		if (seite.hasParent()) {
			tParentSeite = tb._seiteById(seite.getSeite().getParentId());
			if (tParentSeite == null) {
				throw new RuntimeException("Can not transfer page to target branch. Missing parent page.");
			}
		}
		SeiteSO ts = tb._seiteById(id);
		if (ts == null) { // Seite neu anlegen
			ts = tb.getSeiten().createSeite(tParentSeite == null ? tb.getISeite() : tParentSeite, tb, id);
			ts.getSeite().copyFrom_allFields(seite.getSeite());
			ts.saveAll(ts.getSeite().getTitle(), seite.getContent(), seite.getSeite().getVersion(), "",
					MinervaWebapp.factory().getLanguages(), System.currentTimeMillis());
		} else { // Seite überschreiben
			// TODO
			throw new RuntimeException("Seite überschreiben fehlt noch");
		}
	}
}
