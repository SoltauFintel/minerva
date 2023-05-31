package minerva.book;

import minerva.user.UAction;

public class SelectLanguageAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String lang = ctx.pathParam("lang");
        
        user.getUser().setLanguage(lang);
        user.getWorkspace(branch).pull(); // ja, brutal, Seiten-Neusortierung würde auch reichen
        
        ctx.redirect("/b/" + branch);
    }
}
