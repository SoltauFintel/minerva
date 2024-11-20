package minerva.seite;

import java.util.List;

public class PageMenuSupplier {

	public List<PageMenuItem> getMenuItems(PageMenuContext ctx) {
		return List.of( //
			new PageMenuItem("{viewlink}/toggle-favorite", "fav fa-star" + (ctx.isFavorite() ? "" : "-o"), "N.favorite", ctx.isFavorite()),
			new PageMenuItem("{viewlink}/toggle-watch", "fa-bell" + (ctx.isPageWatched() ? "" : "-o"), "N.watchPage", ctx.isPageWatched()),
			new PageMenuItem("{viewlink}/toggle-watch?m=s", "fa-sitemap", "N.watchSubpages", ctx.isSubpagesWatched()),
	        new PageMenuItem(" data-toggle=\"modal\" data-target=\"#tocModal\"", "fa-list-ul", "N.TOC"),
	        new PageMenuItem("{viewlink}/attachments", "fa-paperclip", "Attachments"),
        	new PageMenuItem(ctx.getSeite().isFeatureTree(), "{viewlink}/quick", "fa-rocket", "N.QuicklyCreateFeaturesTitle"),
        	new PageMenuItem("", "", "-"),
        	new PageMenuItem(ctx.isGitlab(), "{viewlink}/history", "fa-history", "N.history"),
            new PageMenuItem(ctx.isAdmin(), " data-toggle=\"modal\" data-target=\"#editorsnoteModal\"", "fa-thumb-tack", "N.editorsNote"),
            new PageMenuItem(ctx.isCustomerVersion(), "{viewlink}/help-keys", "fa-question-circle",
                    "N.helpKeys|(" + ctx.get("helpKeysSize") + ")"),
            new PageMenuItem("{viewlink}/cross-book-links", "fa-share", "N.crossBookLinksMenu", !ctx.getSeite().getSeite().getLinks().isEmpty()),
	        new PageMenuItem("{viewlink}/links", "fa-link", "N.linkAnalysis"),
	        new PageMenuItem("", "", "-"),
	        new PageMenuItem(ctx.isAdmin(), "{viewlink}/html", "fa-code", "N.editHTML"),
	        new PageMenuItem("/w/{branch}/export?seite={id}", "fa-upload", "N.exportPage"),
	        new PageMenuItem("{duplicatelink}", "fa-copy", "N.duplicatePage"),
            new PageMenuItem(!ctx.isCustomerMode() /*recht willkuerliches Verbot*/, "{movelink}", "fa-arrow-circle-right", "N.movePage"), 
            new PageMenuItem(!ctx.isCustomerMode() /*recht willkuerliches Verbot*/, "{deletelink}" , "fa-trash", "N.deletePage|...")
			);
	}
}
