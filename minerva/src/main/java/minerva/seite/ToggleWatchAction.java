package minerva.seite;

public class ToggleWatchAction extends SAction {

    @Override
    protected void execute() {
        user.toggleWatch(id);
        
        ctx.redirect(viewlink);
    }
}
