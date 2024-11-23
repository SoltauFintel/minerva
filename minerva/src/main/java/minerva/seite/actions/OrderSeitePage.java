package minerva.seite;

import minerva.model.SeitenSO;

public class OrderSeitePage extends SPage {
    private OrderSeiteService sv;
    
    @Override
    protected void execute() {
        sv = new OrderSeiteService(ctx, isPOST(), model, branch, //
                bookFolder, id, viewlink, user, seite) {
            @Override
            protected void saveSubpagesAfterReordering(SeitenSO seitenWC) {
                seite.saveSubpagesAfterReordering(seitenWC);
            }
        };
        sv.execute();
    }
    
    @Override
    protected String render() {
        return sv.render();
    }
}
