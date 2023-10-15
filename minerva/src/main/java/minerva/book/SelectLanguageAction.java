package minerva.book;

import minerva.workspace.WAction;

public class SelectLanguageAction extends WAction {

    @Override
    protected void execute() {
        String lang = ctx.queryParam("lang");
        boolean pageMode = "page".equals(ctx.queryParam("m"));
        
        user.selectLanguage(lang, pageMode);

        if (!pageMode) {
            ctx.redirect("/w/" + branch);
        }
    }
}
