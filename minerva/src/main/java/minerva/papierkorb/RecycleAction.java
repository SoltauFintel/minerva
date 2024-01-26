package minerva.papierkorb;

import minerva.workspace.WAction;

/**
 * Papierkorb-Eintrag wiederherstellen
 */
public class RecycleAction extends WAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        String path = workspace.getPapierkorb().pop(id);

        ctx.redirect(path);
    }
}
