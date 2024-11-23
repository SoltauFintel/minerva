package minerva.seite.actions;

import minerva.seite.SAction;

public class ToggleFavoriteAction extends SAction {

    @Override
    protected void execute() {
        user.toggleFavorite(id);
        
        ctx.redirect(viewlink);
    }
}
