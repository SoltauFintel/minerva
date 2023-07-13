package minerva.book;

public class SortTopLevelSeiteAction extends BAction {

    @Override
    protected void execute() {
        book.activateSorted();
        user.log("Sorting activated for top level pages of book " + book.getBook().getFolder());
        ctx.redirect(booklink);
    }
}
