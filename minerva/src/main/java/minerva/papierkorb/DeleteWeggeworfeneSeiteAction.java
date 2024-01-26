package minerva.papierkorb;

import minerva.workspace.WAction;

/**
 * Papierkorb-Eintrag endgültig löschen
 */
public class DeleteWeggeworfeneSeiteAction extends WAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        workspace.getPapierkorb().delete(id);
        
        ctx.redirect("/w/" + branch + "/recycle");
    }
}
