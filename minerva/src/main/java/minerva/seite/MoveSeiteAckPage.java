package minerva.seite;

import minerva.model.SeiteSO;

/**
 * Bestätigungsseite für Seitenverschiebung
 */
public class MoveSeiteAckPage extends SPage {

	@Override
	protected void execute() {
		String parentId = ctx.queryParam("parentid");
		
		SeiteSO parent = book.getSeiten().byId(parentId);

		header(n("movePage"));
		put("parentId", esc(parentId));
		put("parentPageTitle", esc(parent.getTitle()));
		put("movePage1", n("movePage1").replace("$p", esc(parent.getTitle())).replace("$t", esc(seite.getTitle())));
	}
}
