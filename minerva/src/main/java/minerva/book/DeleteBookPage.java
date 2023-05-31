package minerva.book;

public class DeleteBookPage extends BPage {

    @Override
    protected void execute() {
        put("bookTitle", esc(book.getTitle()));
        header(n("deleteBook"));
        
        if ("d".equals(ctx.queryParam("m"))) {
            user.getWorkspace(branch).getBooks().remove(bookFolder);
            ctx.redirect("/b/" + branch);
        }
    }
}
