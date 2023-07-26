package minerva.seite.move;

import minerva.base.StringService;
import minerva.seite.SAction;

public class MoveSeiteAction extends SAction {

    @Override
    protected void execute() {
        String parentId = ctx.queryParam("parentid");
        String folder = ctx.queryParam("folder");

        if (StringService.isNullOrEmpty(folder)) {
            seite.log("Page will be moved to " + parentId);
            seite.move(parentId);
            
            ctx.redirect(viewlink);
        } else {
            seite.log("Page will be moved to book " + folder);
            seite.moveToBook(folder, langs);
            
            ctx.redirect("/s/" + branch + "/" + folder + "/" + esc(id));
        }
    }
}
