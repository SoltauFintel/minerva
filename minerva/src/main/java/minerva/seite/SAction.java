package minerva.seite;

import github.soltaufintel.amalia.spark.Context;
import minerva.book.BAction;
import minerva.model.SeiteSO;

/**
 * Base class for Seite actions
 */
public abstract class SAction extends BAction {
    protected String id;
    protected SeiteSO seite;
    protected String viewlink;

    @Override
    public void init(Context ctx) {
        super.init(ctx);
        id = ctx.pathParam("id");
        viewlink = "/s/" + branch + "/" + bookFolder + "/" + id;

        seite = book.getSeiten().byId(id);
    }
}
