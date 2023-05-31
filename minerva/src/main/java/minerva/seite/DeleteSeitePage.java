package minerva.seite;

import org.pmw.tinylog.Logger;

import minerva.model.SeiteSO;

public class DeleteSeitePage extends SPage {

    @Override
    protected void execute() {
        String title = seite.getTitle();
        String parentId = seite.getSeite().getParentId();

        header(n("deletePage"));

        if ("d".equals(ctx.queryParam("m"))) {
            seite.remove();

            Logger.info("Page deleted (incl. all subpages): " + user.getUser().getLogin() + "/" + branch + "/"
                    + bookFolder + "/" + id + " \"" + title + "\"");

            if (parentId.equals(SeiteSO.ROOT_ID)) {
                ctx.redirect(booklink);
            } else {
                ctx.redirect(viewlink);
            }
        }
    }
}
