package minerva.user.quickbuttons;

import minerva.user.UAction;

public class DeleteQuickbuttonAction extends UAction {

    @Override
    protected void execute() {
        var id = ctx.queryParam("id");
        
        var qb = user.getUser().getQuickbutton(id);
        user.getUser().getQuickbuttons().remove(qb);
        user.saveQuickbuttons();
        
        ctx.redirect("/q/config");
    }
}
