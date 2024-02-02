package minerva.seite;

import org.pmw.tinylog.Logger;

public class PostContentsAction extends SAction {
    
    @Override
    protected void execute() {
        String comment = ctx.formParam("comment");
        int version = Integer.parseInt(ctx.formParam("version"));
        Logger.info(user.getLogin() + " | " + branch + " | received page content #" + id + " | " + version + " | " + comment);
        PostContentsData data = new PostContentsData(branch, bookFolder, id, comment, version);
        for (String lang : langs) {
            String LANG = lang.toUpperCase();
            data.getContent().setString(lang, ctx.formParam("content" + LANG));
            data.getTitle().setString(lang, ctx.formParam("titel" + LANG).trim());
        }
        PostContentsService.set(data);
    }
}
