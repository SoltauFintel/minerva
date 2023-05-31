package minerva.book;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.base.NlsString;

public class EditBookPage extends BPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            NlsString title = new NlsString();
            langs.forEach(lang -> title.setString(lang, ctx.queryParam("bookTitle" + lang)));
            
            book.getBook().setTitle(title);
            book.getBook().setPosition(Integer.parseInt(ctx.queryParam("position")));
            books.sort(); // Position könnte geändert sein
            books.save("edit book " + book.getFolder());

            ctx.redirect("/b/" + branch);
        } else {
            header(n("editBook"));
            langs.forEach(lang -> put("bookTitle" + lang, book.getBook().getTitle().getString(lang)));
            ColumnFormularGenerator gen = new ColumnFormularGenerator(2, 1);
            initColumnFormularGenerator(gen);
            boolean first = true;
            for (String lang : langs) {
                gen.textfield("bookTitle" + lang, n("buchtitel") + " " + lang, 3, first, true);
                first = false;
            }
            putInt("position", book.getBook().getPosition());
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
