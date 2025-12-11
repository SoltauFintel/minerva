package minerva.workspace;

public class ToggleColoredHeadingsAction extends WAction {

    @Override
    protected void execute() {
        user.toggleColoredHeadings();
        
        ctx.redirect("/w/" + branch + "/menu");
    }
}
