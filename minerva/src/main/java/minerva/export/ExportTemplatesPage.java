package minerva.export;

import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
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
            Logger.info(user.getLogin() + " | saved export templates");
            user.log("saved export templates");
            files = new HashMap<>();
            save(ExportTemplatesService.BOOKS, ctx.formParam("books"));
            save(ExportTemplatesService.BOOK, ctx.formParam("book"));
            save(ExportTemplatesService.PAGE, ctx.formParam("page"));
            save(ExportTemplatesService.TEMPLATE, ctx.formParam("template"));
            save(ExportTemplatesService.TEMPLATE_CSS, ctx.formParam("templateCss"));
            save(ExportTemplatesService.PDF_CSS, ctx.formParam("pdfCss"));
            workspace.dao().saveFiles(files, new CommitMessage("Export templates"), workspace);
            ctx.redirect("/w/" + branch);
        } else {
            header(n("exportTemplates"));
            put("books", esc(x.loadTemplate(ExportTemplatesService.BOOKS)));
            put("book", esc(x.loadTemplate(ExportTemplatesService.BOOK)));
            put("page", esc(x.loadTemplate(ExportTemplatesService.PAGE)));
            put("template", esc(x.loadTemplate(ExportTemplatesService.TEMPLATE)));
            put("templateCss", esc(x.loadTemplate(ExportTemplatesService.TEMPLATE_CSS)));
            put("pdfCss", esc(x.loadTemplate(ExportTemplatesService.PDF_CSS)));
        }
    }
    
    private void save(String dn, String content) {
        files.put(workspace.getFolder() + "/" + dn, content);
    }
}
