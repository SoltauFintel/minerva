package minerva.book;

public class ToggleShowAllPagesAction extends BAction {

    @Override
    protected void execute() {
        user.toggleShowAllPages();
        ctx.redirect(booklink);
    }
}
