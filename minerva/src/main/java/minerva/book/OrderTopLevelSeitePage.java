package minerva.book;

import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.actions.OrderSeiteService;

public class OrderTopLevelSeitePage extends BPage {
    private OrderSeiteService sv;

    @Override
    protected void execute() {
        sv = new OrderSeiteService(ctx, isPOST(), model, branch, //
                bookFolder, SeiteSO.ROOT_ID, booklink, user, book.getISeite()) {
            @Override
            protected void saveSubpagesAfterReordering(SeitenSO seiten) {
                book.savePagesAfterReordering(seiten);
            }
        };
        put("viewlink", booklink);
        sv.execute();
    }

    @Override
    protected String render() {
        return sv.render();
    }
}
