package minerva.seite;

public class CancelEditingAction extends SAction {

    @Override
    protected void execute() {
        workspace.onEditing(seite, true); // editing finished
        
        ctx.redirect(viewlink);
    }
}
