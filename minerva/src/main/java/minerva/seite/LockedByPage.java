package minerva.seite;

import minerva.MinervaWebapp;
import minerva.model.UserSO.LoginAndEndTime;

/**
 * Page editing is softly locked. The user can override this.
 */
public class LockedByPage extends SPage {
    
    @Override
    protected void execute() {
        String editlink = "/s-edit/" + branch + "/" + bookFolder + "/" + id;
        LoginAndEndTime lockedBy = user.hasEditingStarted(workspace.getBranch(), id);
        if (lockedBy == null) { // Is page locked softly?
            // User pressed F5 - and now it's not locked anymore. So go to edit page.
            render = false;
            ctx.redirect(editlink);
            return;
        }
        
        header(n("locked.header"));
        put("msg", esc(n("locked.info").replace("$t", seite.getTitle()).replace("$u", MinervaWebapp.factory().login2RealName(lockedBy.getLogin()))));
        put("msg2", esc(n("locked.time").replace("$o", lockedBy.getEndTime())));
        put("editlink", editlink + "?m=cl");
        put("edit", n("locked.edit"));
        put("cancel", n("locked.no-edit"));
    }
}
