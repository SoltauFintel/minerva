package minerva.auth;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Action;
import minerva.base.StringService;
import minerva.base.Tosmap;
import minerva.persistence.filesystem.FileSystemLoginService;

/**
 * Foreign application call this in 2nd step to open online help editor.
 */
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
        MinervaAuth.login1(ctx, FileSystemLoginService.loginUser(login));
        
        ctx.redirect("/");
    }
}
