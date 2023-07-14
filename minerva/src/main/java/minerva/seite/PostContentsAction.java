package minerva.seite;

import org.pmw.tinylog.Logger;

public class PostContentsAction extends SAction {
    
    @Override
    protected void execute() {
        int version = Integer.parseInt(ctx.formParam("version"));
        Logger.info(user.getUser().getLogin() + " | " + branch + " | received page content #" + id);
        PostContentsData data = new PostContentsData(branch, bookFolder, id, version);
        for (String lang : langs) {
            data.getContent().setString(lang, ctx.formParam("content" + lang.toUpperCase()));
        }
        PostContentsService.set(data);
    }
}
