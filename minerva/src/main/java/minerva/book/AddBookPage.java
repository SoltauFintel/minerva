package minerva.book;

import minerva.base.NlsString;
import minerva.user.UPage;

public class AddBookPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        
        user.onlyAdmin();
        
        if (isPOST()) {
            String folder = ctx.queryParam("folder");
            NlsString title = new NlsString();
            langs.forEach(lang -> title.setString(lang, ctx.queryParam("bookTitle" + lang)));
            int position = Integer.parseInt(ctx.queryParam("position"));
            
            user.getCurrentWorkspace().getBooks().createBook(folder, title, langs, position);

            ctx.redirect("/w/" + branch);
        } else {
            header(n("addBook"));
            put("branch", esc(branch));
            putInt("position", user.getWorkspace(branch).getBooks().calculateNextPosition()); 
        }
    }
}
