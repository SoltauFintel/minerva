package minerva.base;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;

public class MinervaError404Page extends Page {

    @Override
    protected void execute() {
        String title = "Minerva error 404";
        put("title", title);
        put("header", title);
        String msg = "Sorry, the page you are looking for is not available.";
        if (!"/fonts/glyphicons-halflings-regular.woff2".equals(ctx.path())) {
            Logger.error("Error 404 rendering path \"" + ctx.path() + "\": " + msg);
        }
        ctx.status(404);
        put("msg", esc(msg)); // for subclasses
        put("p", "The information you are looking for may be in a different location or"
                + " the page no longer exists. Start again from the homepage.");
        MinervaMetrics.ERRORPAGE404.inc();
    }
    
    @Override
    protected String render() {
        try {
            return templates.render(MinervaErrorPage.class.getSimpleName(), model);
        } catch (Exception e) {
            Logger.error(e);
            return "Error while displaying error. See log.";
        }
    }
}
