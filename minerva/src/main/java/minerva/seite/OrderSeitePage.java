package minerva.seite;

import minerva.model.SeitenSO;

/**
 * Es gibt hier 3 Events: startEvent, movedEvent und endEvent. Beim startEvent wird eine Arbeitskopie gemacht,
 * damit beim Abbruch die Original-Seiten nicht verändert werden. Beim movedEvent informiert die GUI über eine
 * einzelne Drag & Drop Aktion = Seitenumordnung. Da gerade bei der Git-Persistenz das Speichern dauert, wird
 * erst im endEvent gespeichert.
 */
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
