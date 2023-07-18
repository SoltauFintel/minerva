package minerva.export;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.workspace.WPage;

/**
 * Edit export HTML and CSS templates
 */
public class ExportTemplatesPage extends WPage {

    @Override
    protected void execute() {
        ExportTemplatesService x = new ExportTemplatesService(workspace);
        if (isPOST()) {
            Logger.info(user.getUser().getLogin() + " | saved export templates");
            user.log("saved export templates");
            x.saveTemplate(ExportTemplatesService.BOOKS, ctx.formParam("books"));
            x.saveTemplate(ExportTemplatesService.BOOK, ctx.formParam("book"));
            x.saveTemplate(ExportTemplatesService.PAGE, ctx.formParam("page"));
            x.saveTemplate(ExportTemplatesService.TEMPLATE, ctx.formParam("template"));
            x.saveTemplate(ExportTemplatesService.TEMPLATE_CSS, ctx.formParam("templateCss"));
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
    
    @Override
    protected String getPage() {
        return "formular/" + super.getPage();
    }
}
