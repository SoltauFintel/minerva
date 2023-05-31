package minerva.seite;

import minerva.user.UAction;

public class AddSeiteAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String parentId = ctx.pathParam("parentid");

        String id = user.createSeite(branch, bookFolder, parentId);

        ctx.redirect("/s-edit/" + branch + "/" + bookFolder + "/" + id);
    }
}
