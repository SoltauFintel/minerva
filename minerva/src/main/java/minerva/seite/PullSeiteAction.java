package minerva.seite;

import minerva.user.UAction;

public class PullSeiteAction extends UAction {

    @Override
    protected void execute() {
        // wenn kurz vorher gespeichert wurde (und das Repo noch im WorkBranch ist), dann scheitert das hier
        
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String id = ctx.pathParam("id");

        user.getWorkspace(branch).pull();

        ctx.redirect("/s/" + branch + "/" + bookFolder + "/" + id);
    }
}
