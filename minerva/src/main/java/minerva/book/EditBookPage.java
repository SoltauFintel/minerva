package minerva.book;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.base.NlsString;
import minerva.model.BooksSO;
import minerva.user.UPage;

public class EditBookPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");

        BooksSO books = user.getWorkspace(branch).getBooks();
        Book book = books.byFolder(bookFolder).getBook();
        
        if (isPOST()) {
            NlsString title = new NlsString();
            langs.forEach(lang -> title.setString(lang, ctx.queryParam("bookTitle" + lang)));
            
            book.setTitle(title);
            book.setPosition(Integer.parseInt(ctx.queryParam("position")));
            books.sort(); // Position könnte geändert sein
            books.save("edit book " + book.getFolder());

            ctx.redirect("/b/" + branch);
        } else {
            header(n("editBook"));
            langs.forEach(lang -> put("bookTitle" + lang, book.getTitle().getString(lang)));
            ColumnFormularGenerator gen = new ColumnFormularGenerator(2, 1);
            boolean first = true;
            for (String lang : langs) {
                gen.textfield("bookTitle" + lang, n("buchtitel") + " " + lang, 3, first, true);
                first = false;
            }
            putInt("position", book.getPosition()); 
            TemplatesInitializer.fp.setContent(gen
                    .textfield("position", "Position", 1, false, true)
                    .getHTML("/b/" + branch + "/" + bookFolder + "/edit", "/b/" + branch));
        }
    }
    
    @Override
    protected String getPage() {
        return "formular/AddBookPage";
    }
}
