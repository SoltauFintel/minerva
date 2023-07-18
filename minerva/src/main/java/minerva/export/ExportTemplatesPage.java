package minerva.export;

import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.git.CommitMessage;
import minerva.workspace.WPage;

/**
 * Edit export HTML and CSS templates
 */
public class ExportTemplatesPage extends WPage {
    private ExportTemplatesService x;
    private Map<String, String> files;
    
    @Override
    protected void execute() {
        x = new ExportTemplatesService(workspace);
        if (isPOST()) {
            Logger.info(user.getUser().getLogin() + " | saved export templates");
            user.log("saved export templates");
            files = new HashMap<>();
            save(ExportTemplatesService.BOOKS, ctx.formParam("books"));
            save(ExportTemplatesService.BOOK, ctx.formParam("book"));
            save(ExportTemplatesService.PAGE, ctx.formParam("page"));
            save(ExportTemplatesService.TEMPLATE, ctx.formParam("template"));
            save(ExportTemplatesService.TEMPLATE_CSS, ctx.formParam("templateCss"));
            user.dao().saveFiles(files, new CommitMessage("Export templates"), workspace);
            ctx.redirect("/b/" + branch);
        } else {
            header(n("exportTemplates"));
            put("books", esc(x.loadTemplate(ExportTemplatesService.BOOKS)));
            put("book", esc(x.loadTemplate(ExportTemplatesService.BOOK)));
            put("page", esc(x.loadTemplate(ExportTemplatesService.PAGE)));
            put("template", esc(x.loadTemplate(ExportTemplatesService.TEMPLATE)));
            put("templateCss", esc(x.loadTemplate(ExportTemplatesService.TEMPLATE_CSS)));
            
            ColumnFormularGenerator gen = new ColumnFormularGenerator(1, 1);
            initColumnFormularGenerator(gen);
            TemplatesInitializer.fp.setContent(gen
                    .textarea("books", n("booksOverview"), 10, 10, true, true)
                    .textarea("book", n("book"), 10, 10, false, true)
                    .textarea("page", n("page"), 10, 10, false, true)
                    .textarea("template", n("template"), 10, 10, false, true)
                    .textarea("templateCss", "CSS", 10, 10, false, true)
                    .getHTML("/w/" + branch + "/export-templates", "/b/" + branch));
        }
    }
    
    private void save(String dn, String content) {
        files.put(workspace.getFolder() + "/" + dn, content);
    }

    @Override
    protected String getPage() {
        return "formular/" + super.getPage();
    }
}
