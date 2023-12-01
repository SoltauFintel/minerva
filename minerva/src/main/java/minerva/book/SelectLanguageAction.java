package minerva.book;

import minerva.workspace.WAction;

public class SelectLanguageAction extends WAction {

    @Override
    protected void execute() {
    	String mode = ctx.queryParam("m");
		if ("toggle".equals(mode)) {
			user.toggleGuiLanguage();
			
			ctx.redirect(ctx.req.headers("Referer"));
		} else { // set language
	        String lang = ctx.queryParam("lang");
			boolean pageMode = "page".equals(mode);

			user.selectLanguage(lang, pageMode);
			
			if (!pageMode) {
				ctx.redirect("/w/" + branch);
			}
		}
    }
}
