package minerva.seite;

public class ToggleFavoriteAction extends SAction {

    @Override
    protected void execute() {
        user.toggleFavorite(id);
        
        ctx.redirect(viewlink);
    }
}
