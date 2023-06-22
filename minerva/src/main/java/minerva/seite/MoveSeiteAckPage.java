package minerva.seite;

import minerva.model.BookSO;
import minerva.model.SeiteSO;

/**
 * Bestätigungsseite für Seitenverschiebung
 */
public class MoveSeiteAckPage extends SPage {

    @Override
    protected void execute() {
        String parentId = ctx.queryParam("parentid");
        String folder = ctx.queryParam("folder");
        
        String text;
        header(n("movePage"));
        if (folder == null || folder.isEmpty()) {
            String parentTitle;
            if (SeiteSO.ROOT_ID.equals(parentId)) {
                parentTitle = book.getTitle();
            } else {
                SeiteSO parent = book.getSeiten().byId(parentId);
                parentTitle = parent.getTitle();
            }
    
            put("moveToBook", false);
            put("parentId", esc(parentId));
            text = n("movePage1")
                    .replace("$t", esc(seite.getTitle()))
                    .replace("$p", esc(parentTitle));
        } else {
            put("moveToBook", true);
            put("folder", esc(folder));
            BookSO targetBook = books.byFolder(folder);
            text = n("movePage2").replace("$b", esc(targetBook.getTitle())).replace("$t", esc(seite.getTitle()));
        }
        put("movePageText", text);
    }
}
