package minerva.book;

public class SelectNoneAction extends BAction {

    @Override
    protected void execute() {
        book.clearMultiSelect();
        
        ctx.redirect("/b/" + branch + "/" + bookFolder + "/select");
    }
}
