package minerva.book;

import minerva.model.BookSO;
import minerva.user.UAction;

public class SelectLanguageAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String lang = ctx.pathParam("lang");
        
        user.getUser().setLanguage(lang);
        for (BookSO book : user.getWorkspace(branch).getBooks()) {
            book.getSeiten().sortAll();
        }
        
        ctx.redirect("/b/" + branch);
    }
}
