package minerva.user.quickbuttons;

import minerva.user.UAction;

/**
 * Quickbuttons: toggle onlyMe
 */
public class ToggleQuickbuttonAction extends UAction {

    @Override
    protected void execute() {
        var id = ctx.queryParam("id");
        
        var qb = user.getUser().getQuickbutton(id);
        qb.setOnlyMe(!qb.isOnlyMe());
        user.saveQuickbuttons();
        
        ctx.redirect("/q/config");
    }
}
