package minerva.seite;

import org.pmw.tinylog.Logger;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.user.UPage;

public class DeleteSeitePage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String id = ctx.pathParam("id");

        BookSO book = user.getWorkspace(branch).getBooks().byFolder(bookFolder);
        SeiteSO seite = book.getSeiten().byId(id);
        String title = seite.getTitle();
        String parentId = seite.getSeite().getParentId();

        put("branch", branch);
        put("bookFolder", bookFolder);
        put("folder", bookFolder);
        put("id", id); // TODO kann weg wenn SPage
        put("pagetitle", esc(title)); // TODO kann weg wenn SPage
        header("Seite löschen"); // TODO NLS

        if ("d".equals(ctx.queryParam("m"))) {
            seite.remove();

            Logger.info("Seite (inkl. aller Unterseiten) gelöscht: " + user.getUser().getLogin() + "/" + branch + "/"
                    + bookFolder + "/" + id + " \"" + title + "\"");

            if (parentId.equals(SeiteSO.ROOT_ID)) {
                ctx.redirect("/b/" + branch + "/" + bookFolder);
            } else {
                ctx.redirect("/s/" + branch + "/" + bookFolder + "/" + parentId);
            }
        }
    }
}
