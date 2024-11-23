package minerva.seite.actions;

import minerva.model.UserSO.LoginAndEndTime;
import minerva.seite.SPage;
import minerva.user.UserAccess;

/**
 * Page editing is softly locked. The user can override this in 2 stages.
 */
public class LockedByPage extends SPage {
    
    @Override
    protected void execute() {
        final String editlink = "/s-edit/" + branch + "/" + bookFolder + "/" + id;
        LoginAndEndTime lockedBy = user.hasEditingStarted(workspace.getBranch(), id);
        if (lockedBy == null) { // Is page locked softly?
            // User pressed F5 - and now it's not locked anymore. So go to edit page.
            render = false;
            ctx.redirect(editlink);
            return;
        }
        
        String username = UserAccess.login2RealName(lockedBy.getLogin());
        String goOnLink = editlink + "?m=cl";
        
        if ("2".equals(ctx.queryParam("s"))) { // Stage 2
            fill(n("locked.header") + " (2)",
                    n("locked.info2").replace("$t", seite.getTitle()).replace("$u", username),
                    "",
                    goOnLink,
                    n("locked.edit2"));
        } else { // Stage 1
            boolean itsMe = user.getLogin().equals(lockedBy.getLogin());
            String x = itsMe ? ".self" : "";
            fill(n("locked.header"),
                    n("locked.info1" + x).replace("$t", seite.getTitle()).replace("$u", username),
                    n("locked.smallinfo1" + x).replace("$o", lockedBy.getEndTime()),
                    itsMe ? goOnLink : viewlink + "/locked?s=2",
                    n("locked.edit1" + x));
        }
    }
    
    private void fill(String header, String info, String smallinfo, String editlink, String editBtnLabel) {
        header(header);
        put("info", esc(info));
        put("smallinfo", esc(smallinfo));
        put("editlink", editlink);
        put("edit", editBtnLabel);
        put("cancel", n("locked.no-edit"));
    }
}
