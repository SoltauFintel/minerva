package minerva.book;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.web.action.IdAndLabel;
import minerva.base.NlsString;
import minerva.book.EditBookPage.BookTypeIAL;
import minerva.user.UPage;

public class AddBookPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        
        onlyAdmin();
        
        if (isPOST()) {
            String folder = ctx.queryParam("folder");
            NlsString title = new NlsString();
            langs.forEach(lang -> title.setString(lang, ctx.queryParam("bookTitle" + lang)));
            BookType type = BookType.valueOf(ctx.queryParam("type"));
            int position = Integer.parseInt(ctx.queryParam("position"));
            
            user.getCurrentWorkspace().getBooks().createBook(folder, title, langs, type, position);

            ctx.redirect("/w/" + branch);
        } else {
            header(n("addBook"));
            put("branch", esc(branch));
            putInt("position", user.getWorkspace(branch).getBooks().calculateNextPosition()); 

            List<IdAndLabel> bookTypes = new ArrayList<>();
            for (BookType bookType : BookType.values()) {
                bookTypes.add(new BookTypeIAL(bookType, user.getGuiLanguage()));
            }
            combobox_idAndLabel("bookTypes", bookTypes, BookType.PUBLIC.name(), false);
        }
    }
}
