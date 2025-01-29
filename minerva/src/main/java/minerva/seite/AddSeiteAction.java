package minerva.seite;

import minerva.book.BAction;
import minerva.model.BookSO;

public class AddSeiteAction extends BAction {
	public static AddSeiteAllowed addSeiteAllowed = (book, parent) -> {};

    @Override
    protected void execute() {
        String parentId = ctx.pathParam("parentid");

        addSeiteAllowed.checkIfAllowed(book, parentId);
        String id = user.createSeite(branch, bookFolder, parentId);

        ctx.redirect("/s-edit/" + esc(branch) + "/" + esc(bookFolder) + "/" + esc(id));
    }
    
    public interface AddSeiteAllowed {
    	
    	void checkIfAllowed(BookSO book, String parentId);
    }
}
