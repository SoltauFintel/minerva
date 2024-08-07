package minerva.seite;

import github.soltaufintel.amalia.spark.Context;
import minerva.book.BPage;
import minerva.model.SeiteSO;

/**
 * Base class for Seite pages
 */
public abstract class SPage extends BPage {
    protected String id;
    protected SeiteSO seite;
    protected String viewlinkWithoutId;
    protected String viewlink;
    
    @Override
    public void init(Context ctx) {
        super.init(ctx);
        id = ctx.pathParam("id");
        viewlinkWithoutId = "/s/" + esc(branch) + "/" + esc(bookFolder) + "/";
        viewlink = viewlinkWithoutId + esc(id);

        seite = getSeite();
        
        put("id", id);
        put("pageTitle", seite == null ? "" : esc(seite.getTitle()));
        put("viewlink", viewlink);
    }
    
    protected SeiteSO getSeite() {
        return book.seiteById(id);
    }
}
