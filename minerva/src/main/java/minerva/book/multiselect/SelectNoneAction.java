package minerva.book.multiselect;

import minerva.book.BAction;

public class SelectNoneAction extends BAction {

    @Override
    protected void execute() {
        book.clearMultiSelect();
        
        ctx.redirect("/b/" + branch + "/" + bookFolder + "/select");
    }
}
