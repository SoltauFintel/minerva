package minerva.book;

import minerva.MinervaWebapp;

public class DeleteBookPage extends BPage {

    @Override
    protected void execute() {
        if (MinervaWebapp.factory().isCustomerVersion()) {
            throw new RuntimeException("This operation is not allowed.");
        }
        onlyAdmin();

        put("bookTitle", esc(book.getTitle()));
        header(n("deleteBook"));
        
        if ("d".equals(ctx.queryParam("m"))) {
            user.getWorkspace(branch).getBooks().remove(bookFolder);
            ctx.redirect("/w/" + branch);
        }
    }
}
