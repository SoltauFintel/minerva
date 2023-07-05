package minerva.auth;

import github.soltaufintel.amalia.web.action.Action;
import minerva.base.StringService;
import minerva.user.User;

public class Book6StartAction extends Action {

    @Override
    protected void execute() {
        String a = ctx.queryParam("a");
        if (!"abc".equals(a)) {
            // wrong key
            ctx.redirect("/message?m=4");
            return;
        }
        // ~duplicate code >>
        String folder = System.getenv("MINERVA_USERFOLDER");
        String login = Book6LoginAction._user; // TODO künftig nicht mehr über static var
        if (StringService.isNullOrEmpty(folder)) {
            folder = login;
        }
        User user = new User(login, folder);
        // <<
        MinervaAuth.login1(ctx, user);
        
        ctx.redirect("/");
    }
}
