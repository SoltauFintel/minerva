package minerva.seite.actions;

import minerva.seite.SAction;

public class SortSeiteAction extends SAction {

    @Override
    protected void execute() {
        user.onlyAdmin();
        seite.activateSorted();
        seite.log("Sorting activated.");
        ctx.redirect(viewlink);
    }
}
