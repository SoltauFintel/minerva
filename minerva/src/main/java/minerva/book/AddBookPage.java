package minerva.book;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
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
            ColumnFormularGenerator gen = new ColumnFormularGenerator(2, 1);
            initColumnFormularGenerator(gen);
            boolean first = true;
            for (String lang : langs) {
                gen.textfield("bookTitle" + lang, n("buchtitel") + " " + lang, 3, first, false);
                first = false;
            }
            putInt("position", user.getWorkspace(branch).getBooks().calculateNextPosition()); 
            TemplatesInitializer.fp.setContent(gen
                    .textfield("folder", n("ordner"), 3, false, false)
                    .textfield("position", "Position", 1, false, true)
                    .getHTML(model, "/b/" + branch + "/add", "/w/" + branch));
        }
    }
    
    @Override
    protected String getPage() {
        return "formular/" + super.getPage();
    }
}
