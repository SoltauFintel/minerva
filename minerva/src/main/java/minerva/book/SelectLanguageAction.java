package minerva.book;

import minerva.MinervaWebapp;
import minerva.user.UAction;
import minerva.user.UserSettings;

public class SelectLanguageAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String lang = ctx.queryParam("lang");
        String m = ctx.queryParam("m");
        
        if (!MinervaWebapp.factory().getLanguages().contains(lang)) {
            throw new RuntimeException("Illegal language value!");
        }

        boolean isPage = "page".equals(m);

        UserSettings us = user.loadUserSettings();
        us.setPageLanguage(lang);
        if (!isPage) {
            us.setGuiLanguage(lang);
        }
        user.saveUserSettings(us);

        if (isPage) { // page language
            user.getUser().setPageLanguage(lang);
        } else { // GUI language, but also change page language
            user.getUser().setGuiLanguage(lang);
            user.getUser().setPageLanguage(lang);
            ctx.redirect("/b/" + branch);
        }
    }
}
