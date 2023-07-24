package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Action;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.Tosmap;
import minerva.user.User;

public class Book6StartAction extends Action {

    @Override
    protected void execute() {
        String a = ctx.queryParam("a");
        if (StringService.isNullOrEmpty(a)) { // illegal key
            Logger.error("Book6StartAction: illegal key: " + a);
            ctx.redirect("/message?m=4");
            return;
        }
        String login = (String) Tosmap.get(a);
        if (login == null) { // wrong or expired key
            Logger.error("Book6StartAction: wrong or expired key: " + a);
            ctx.redirect("/message?m=4");
            return;
        }
        // ~duplicate code>>
        String folder = MinervaWebapp.factory().getConfig().getUserFolder();
        if (folder.isEmpty()) {
            folder = login;
        }
        User user = new User(login, folder);
        // <<
        MinervaAuth.login1(ctx, user);
        
        ctx.redirect("/");
    }
}
