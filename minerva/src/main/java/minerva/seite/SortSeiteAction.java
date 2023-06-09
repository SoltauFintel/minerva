package minerva.seite;

public class SortSeiteAction extends SAction {

    @Override
    protected void execute() {
        user.onlyAdmin();
        seite.activateSorted();
        seite.log("Sorting activated.");
        ctx.redirect(viewlink);
    }
}
