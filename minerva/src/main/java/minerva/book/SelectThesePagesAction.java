package minerva.book;

import org.pmw.tinylog.Logger;

/**
 * Seiten anhand Buch und tag auswählen
 */
public class SelectThesePagesAction extends BAction {

    @Override
    protected void execute() {
        String tag = ctx.queryParam("tag");

        Logger.info(book.getUser().getLogin() + " | SelectThesePagesAction | book: " + bookFolder + " | tag: " + tag);

        book.saveMultiSelectByTag(tag);

        ctx.redirect("/b/" + branch + "/" + bookFolder + "/select");
    }
}
