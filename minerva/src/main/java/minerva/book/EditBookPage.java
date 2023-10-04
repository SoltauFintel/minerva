package minerva.book;

import minerva.base.NlsString;

public class EditBookPage extends BPage {

    @Override
    protected void execute() {
        user.onlyAdmin();
        
        if (isPOST()) {
            NlsString title = new NlsString();
            langs.forEach(lang -> title.setString(lang, ctx.queryParam("bookTitle" + lang)));
            
            book.getBook().setTitle(title);
            book.getBook().setPosition(Integer.parseInt(ctx.queryParam("position")));
            books.sort(); // Position könnte geändert sein
            books.save(book.cm("edit book"));
            user.log("Book saved. " + book.getBook().getFolder());

            ctx.redirect("/w/" + branch);
        } else {
            header(n("editBook"));
            langs.forEach(lang -> put("bookTitle" + lang, book.getBook().getTitle().getString(lang)));
            putInt("position", book.getBook().getPosition());
        }
    }
}
