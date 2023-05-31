package minerva.seite;

public class MoveSeiteAction extends SAction {

    @Override
    protected void execute() {
        String parentId = ctx.queryParam("parentid");
        String folder = ctx.queryParam("folder");

        if (folder == null || folder.isEmpty()) {
            seite.moveToBook(folder);
            
            ctx.redirect("/s/" + branch + "/" + folder + "/" + esc(id));
        } else {
            seite.move(parentId);
            
            ctx.redirect(viewlink);
        }
    }
}
