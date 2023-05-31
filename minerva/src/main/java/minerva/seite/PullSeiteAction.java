package minerva.seite;

public class PullSeiteAction extends SAction {

    @Override
    protected void execute() {
        // wenn kurz vorher gespeichert wurde (und das Repo noch im WorkBranch ist), dann scheitert das hier
        
        user.getWorkspace(branch).pull();

        ctx.redirect(viewlink);
    }
}
