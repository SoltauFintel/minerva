package minerva.workspace;

public class ActivateFSModeAction extends WAction {

    @Override
    protected void execute() {
        user.activateDelayedPush(branch);
        ctx.redirect("/w/" + branch);
    }
}
