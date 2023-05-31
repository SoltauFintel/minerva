package minerva.seite;

public class SortSeiteAction extends SAction {

    @Override
    protected void execute() {
        seite.activateSorted();
        
        ctx.redirect(viewlink);
    }
}
