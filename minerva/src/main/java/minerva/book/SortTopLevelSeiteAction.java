package minerva.book;

public class SortTopLevelSeiteAction extends BAction {

	@Override
	protected void execute() {
		book.activateSorted();
		ctx.redirect(booklink);
	}
}
