package minerva.book;

import minerva.MinervaWebapp;
import minerva.user.UAction;

public class SelectLanguageAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String lang = ctx.queryParam("lang");
        String m = ctx.queryParam("m");
        
        if (!MinervaWebapp.factory().getLanguages().contains(lang)) {
            throw new RuntimeException("Illegal language value!");
        }

        if ("page".equals(m)) { // page language
            user.getUser().setPageLanguage(lang);
        } else { // GUI language, but also change page language
            user.getUser().setGuiLanguage(lang);
            user.getUser().setPageLanguage(lang);
            ctx.redirect("/b/" + branch);
        }
    }
}
