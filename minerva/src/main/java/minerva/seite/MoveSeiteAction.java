package minerva.seite;

import minerva.base.StringService;

public class MoveSeiteAction extends SAction {

    @Override
    protected void execute() {
        String parentId = ctx.queryParam("parentid");
        String folder = ctx.queryParam("folder");

        if (StringService.isNullOrEmpty(folder)) {
            seite.move(parentId);
            
            ctx.redirect(viewlink);
        } else {
            seite.moveToBook(folder, langs);
            
            ctx.redirect("/s/" + branch + "/" + folder + "/" + esc(id));
        }
    }
}
