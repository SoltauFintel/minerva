package minerva.seite;

import minerva.book.BAction;

public class AddSeiteAction extends BAction {

    @Override
    protected void execute() {
        String parentId = ctx.pathParam("parentid");

        String id = user.createSeite(branch, bookFolder, parentId);

        ctx.redirect("/s-edit/" + esc(branch) + "/" + esc(bookFolder) + "/" + esc(id));
    }
}
